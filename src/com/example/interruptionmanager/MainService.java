package com.example.interruptionmanager;

import java.io.IOException;
import java.io.ObjectOutputStream.PutField;
import java.util.ArrayList;

import com.example.interruptionProperties.ActionObject;
import com.example.interruptionProperties.Interrupter;
import com.example.interruptionProperties.NotificationType;
import com.example.interruptionProperties.Situation;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class MainService extends Service {

	private static int updateNrOfIterations = 10;
	private static double updateIterationDuration = 0.5; // in seconds
	private static double updateInterval = 180; // in seconds

	private MediaRecorder mRecorder;
	private SensorManager mSensorManager;
	private Sensor mLight;
	private Sensor mActivity;
	private AudioManager audio;
	private Handler handler;
	private AnalysisModel aModel;
	private SupportModel sModel;
	private String interrupterID;
	private String notificationType;

	private double lastAveSound = 0;
	private double lastAveActivity = 0;
	private double lastAveLight = 0;
	double currentActivityValue = 0;
	double currentLightValue = 0;

	ArrayList<Situation> situations;
	ArrayList<Interrupter> interrupters;
	ArrayList<NotificationType> notifications;
    
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		if(action.equals("android.provider.Telephony.SMS_RECEIVED")){
				Bundle extras = intent.getExtras();
    			//createNotification("Received SMS", "You received an SMS from "+extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)+". That's all.", 0);
			}
    		if(action.equals("android.intent.action.PHONE_STATE")){
				Bundle extras = intent.getExtras();
				if (extras != null) {
					String state = extras.getString(TelephonyManager.EXTRA_STATE);
					Log.d("LOG", state);
					if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
						handleInterruption("call", extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER));
						Log.d("LOG", extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER));
						//sendSMS(extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER), "Thomas is in a meeting right now. He will call you back today.");
					}
				}
			}     
    	}
    };
    
    // Sensor listener for accelerometer and light
    private SensorEventListener listener = new SensorEventListener() {
    	private double xGravity, yGravity, zGravity;
    	
		public void onSensorChanged(SensorEvent e) {
			if (e.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
				currentActivityValue = Math.pow(e.values[0] - xGravity, 2);
				currentActivityValue += Math.pow(e.values[1] - yGravity, 2);
				currentActivityValue += Math.pow(e.values[2] - zGravity, 2);
			} else if (e.sensor.getType()==Sensor.TYPE_LIGHT) {
				currentLightValue = e.values[0];
			} else if (e.sensor.getType() == Sensor.TYPE_GRAVITY) {
				xGravity = e.values[0];
				yGravity = e.values[1];
				zGravity = e.values[2];
			}
		}
        
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
        	// Not used
        }
	};
    
    private Thread averageSoundValue = new Thread() {
		
		@Override
		public void run() {
			int i = 0;
			double totalSoundValue = 0;
			double totalActivityValue = 0;
			double totalLightValue = 0;
						
			while (i < updateNrOfIterations) {
				if (isInterrupted()) return;
				totalSoundValue += getAmplitude();
				totalActivityValue += currentActivityValue;
				totalLightValue += currentLightValue;
				i++;
				try {
					sleep((int)(updateIterationDuration * 1000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			lastAveSound = totalSoundValue / i;
			lastAveActivity = totalActivityValue / i;
			lastAveLight = totalLightValue / i;

			Log.d("LOG", "Sound: " + String.valueOf(lastAveSound));
			Log.d("LOG", "Activity: " + String.valueOf(lastAveActivity));
			Log.d("LOG", "Light: " + String.valueOf(lastAveLight));		
			
			aModel.updateSensorValues(lastAveSound, lastAveActivity, lastAveLight);
			
			// Call itself after updateInterval seconds.
			handler.removeCallbacks(averageSoundValue);
			if (isInterrupted()) return;
			handler.postDelayed(averageSoundValue, (int)(updateInterval * 1000));
		}
	};
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d("MainService", "onCreate started");
		aModel = new AnalysisModel();
		sModel = new SupportModel();
		
		createData();
		setupCallListener();
		setupMic();
		
		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mSensorManager.registerListener(listener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(listener, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(listener, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),SensorManager.SENSOR_DELAY_UI);
		
		handler = new Handler();
		handler.removeCallbacks(averageSoundValue);
		handler.postDelayed(averageSoundValue, 1000);
		
		//handleInterruption("call", "5554");
		Intent intent = new Intent("com.example.interruptionmanager.ADAPTATIONACTIVITY")
		.putExtra("situation", "At home working")
		.putExtra("interrupter", "5554")
		.putExtra("notification", "call");
		
		createNotification("Received SMS", "Testing adaptation activity", 0, intent);
		Log.d("MainService", "onCreate ended");
	}

	private void createData() {
		// TODO Auto-generated method stub
		// id, weight, cost, benefit
		// situations are defined in res/xml/preferences.xml
		situations = new ArrayList<Situation>();
        situations.add(new Situation("1", 1, 9, 1)); // At home sleeping
        situations.add(new Situation("2", 1, 2, 5)); // At home relaxing
        situations.add(new Situation("3", 1, 7, 3)); // At home working
        situations.add(new Situation("4", 1, 10, 1)); // At work in meeting
        situations.add(new Situation("5", 1, 8, 0)); // At work at desk
        situations.add(new Situation("6", 1, 4, 3)); // On bike
        situations.add(new Situation("7", 1, 8, 2)); // In car
        situations.add(new Situation("8", 1, 1, 6)); // In public transport
        
		interrupters = new ArrayList<Interrupter>();
		interrupters.add(new Interrupter("+31622725339", 1, 0, 0));
		interrupters.add(new Interrupter("+31659059324", 1, 0, 0));
		interrupters.add(new Interrupter("15555215558", 1, 0, 0));
        
		notifications = new ArrayList<NotificationType>();
		notifications.add(new NotificationType("call", 1, 6, 3));
		notifications.add(new NotificationType("sms", 1, 3, 1));

		aModel.setData(situations, interrupters, notifications);
		sModel.setData(situations, interrupters, notifications);
	}

	@Override
	public void onDestroy() {
		handler.removeCallbacks(averageSoundValue);
		mSensorManager.unregisterListener(listener);
		averageSoundValue.interrupt();
		mRecorder.stop();
		mRecorder.release();
		
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d("LOG", "onDestroy");
	}
	
	@Override
	public void onStart(Intent i, int startid) {
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		mActivity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	private void handleInterruption(String notificationType, String interrupterID) {
		Log.d("MainService", "handleInterruption started");
		int problemState = 0;
		this.interrupterID = interrupterID;
		this.notificationType = notificationType;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		aModel.setSituation(prefs.getString("pref_situation", "0"));
		aModel.setSettings(audio.getStreamVolume(AudioManager.STREAM_RING), audio.getRingerMode());
		problemState = aModel.detectProblemState(notificationType, interrupterID);
		Log.d("MainService", "Problem state: "+String.valueOf(problemState));
		
		if (problemState == AnalysisModel.UNWANTED_INTERRUPTION || problemState == AnalysisModel.MISSING_INTERRUPTION) {
			sModel.setProblemState(problemState);
			sModel.setPIV(aModel.getPIV());
			
			performAction(sModel.getAction());
		}
		Log.d("MainService", "handleInterruption ended");
	}

	private void performAction(ActionObject action) {
		Log.d("LOG", "Performing action");
		setSoundSettings(action.getSoundAction());
		setVibrationSetting(action.getVibrateAction());
		
		if (action.getSendSMS() == 1) sendSMS(interrupterID, "Message");
	}

	private void setupCallListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.PHONE_STATE");
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		
		registerReceiver(receiver, filter);
	}
	
	private void setSoundSettings(int level) {
		// TODO: make mapping from level to actual settings.
//		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING); // current volume
//		audio.setRingerMode(AudioManager.RINGER_MODE_SILENT); // Silent
//		audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); // Vibrate
//		audio.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_LOWER, 0); // volume down
//		audio.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, 0); // volume up

		// TODO: make mapping from level to actual settings.
		if (level == 0 && audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
			audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		} else {
			audio.setStreamVolume(AudioManager.STREAM_RING, level, 0);
		}
	}
	
	private void setVibrationSetting(int level) {
		// TODO: make mapping from level to actual settings.
		Log.d("LOG", "vibration adaptation: " + String.valueOf(level));
		switch (level) {
		case 0:
			if (audio.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			break;
		case 1:
			if (audio.getRingerMode() == AudioManager.RINGER_MODE_SILENT) audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); // Vibrate
			break;
		case 2:
			audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); // Vibrate
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = { 500, 500};
			v.vibrate(pattern, -1);

		default:
			break;
		}
	}

	private void createNotification(String contentTitle, String contentText, int id, Intent intent) {
		// Create notification
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(contentTitle)
		        .setContentText(contentText);
		
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		mBuilder.setContentIntent(pIntent);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(id, mBuilder.build());
	}
	
    private void sendSMS(String phoneNumber, String message) {        
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
 
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
            new Intent(SENT), 0);
 
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
            new Intent(DELIVERED), 0);
 
        // When the SMS has been sent
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", 
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));
 
        // When the SMS has been delivered
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED));        
 
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);        
    }
    


	private void setupMic() {
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile("/dev/null");
			try {
				mRecorder.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mRecorder.start();
		}
	}

	public double getAmplitude() {
		if (mRecorder != null)
			return (mRecorder.getMaxAmplitude() / 2730.0);
		else
			return 0;
	}
}
