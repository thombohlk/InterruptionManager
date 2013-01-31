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
import android.content.res.Resources;
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

	private static int updateNrOfIterations = 1;
	private static double updateIterationDuration = 0.5; // in seconds
	private static double updateInterval = 1; // in seconds

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
	private String situation;
	private int problemState;

	private double lastAveSound = 0;
	private double lastAveActivity = 0;
	private double lastAveLight = 0;
	double currentActivityValue = 0;
	double currentLightValue = 0;

	ArrayList<Situation> situations;
	ArrayList<Interrupter> interrupters;
	ArrayList<NotificationType> notifications;
    
    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		if(action.equals("android.provider.Telephony.SMS_RECEIVED")){
				Bundle extras = intent.getExtras();
				Log.d("LOG", "Receiving incomming call with phonenumber: " + String.valueOf(extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)));
				handleInterruption("sms", extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER));
			}
    		if(action.equals("android.intent.action.PHONE_STATE")){
				Bundle extras = intent.getExtras();
				if (extras != null) {
					String state = extras.getString(TelephonyManager.EXTRA_STATE);
					if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
						Log.d("LOG", "Receiving incomming call with phonenumber: " + String.valueOf(extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)));
						handleInterruption("call", extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER));
						//sendSMS(extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER), "Thomas is in a meeting right now. He will call you back today.");
					}
				}
			}     
    	}
    };

	private void setupCallListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.PHONE_STATE");
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		
		registerReceiver(notificationReceiver, filter);
	}
    
    private final BroadcastReceiver adaptationReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		Bundle extras = intent.getExtras();
    		if(action.equals(AdaptationActivity.INCREASE_SIT)){
    			for (int i = 0; i < situations.size(); i++) {
    				if (situations.get(i).id.equals(extras.getString("situation"))) {
						Log.d("TRACE", "Situation before: benefit = "+String.valueOf(situations.get(i).benefit)+", cost = "+String.valueOf(situations.get(i).cost));
    					situations.get(i).benefit = Math.min(10, Math.max(0, situations.get(i).benefit + extras.getInt("situationBenefit")));
    					situations.get(i).cost = Math.min(10, Math.max(0, situations.get(i).cost + extras.getInt("situationCost")));
						Log.d("TRACE", "Situation after: benefit = "+String.valueOf(situations.get(i).benefit)+", cost = "+String.valueOf(situations.get(i).cost));
    				}
				}
    			for (int i = 0; i < interrupters.size(); i++) {
					if (interrupters.get(i).id.equals(extras.getString("interrupter"))) {
						Log.d("TRACE", "Interrupter before: benefit = "+String.valueOf(interrupters.get(i).benefit)+", cost = "+String.valueOf(interrupters.get(i).cost));
						interrupters.get(i).benefit = Math.min(10, Math.max(0, interrupters.get(i).benefit + extras.getInt("interrupterBenefit")));
						interrupters.get(i).cost = Math.min(10, Math.max(0, interrupters.get(i).cost + extras.getInt("interrupterCost")));
						Log.d("TRACE", "Interrupter after: benefit = "+String.valueOf(interrupters.get(i).benefit)+", cost = "+String.valueOf(interrupters.get(i).cost));
					}
				}
				for (int i = 0; i < notifications.size(); i++) {
					if (notifications.get(i).id.equals(extras.getString("notification"))) {
						Log.d("TRACE", "Notification before: benefit = "+String.valueOf(notifications.get(i).benefit)+", cost = "+String.valueOf(notifications.get(i).cost));
						notifications.get(i).benefit = Math.min(10, Math.max(0, notifications.get(i).benefit + extras.getInt("notificationBenefit")));
						notifications.get(i).cost = Math.min(10, Math.max(0, notifications.get(i).cost + extras.getInt("notificationCost")));
						Log.d("TRACE", "Notification after: benefit = "+String.valueOf(notifications.get(i).benefit)+", cost = "+String.valueOf(notifications.get(i).cost));
					}
				}
    					
    			Toast.makeText(context, String.valueOf(extras.getInt("situationBenefit")), Toast.LENGTH_SHORT).show();
			}   
    	}
    };

	private void setupAdaptationListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(AdaptationActivity.INCREASE_SIT);
		registerReceiver(adaptationReceiver, filter);
	}
    
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

			//Log.d("LOG", "Sound: " + String.valueOf(lastAveSound));
			//Log.d("LOG", "Activity: " + String.valueOf(lastAveActivity));
			//Log.d("LOG", "Light: " + String.valueOf(lastAveLight));		

			aModel.updateSensorValues(lastAveSound, lastAveActivity, lastAveLight);
			sModel.updateSensorValues(lastAveSound, lastAveActivity, lastAveLight);
			
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
		setupAdaptationListener();
		setupMic();
		
		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mSensorManager.registerListener(listener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(listener, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(listener, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),SensorManager.SENSOR_DELAY_UI);
		
		handler = new Handler();
		handler.removeCallbacks(averageSoundValue);
		handler.postDelayed(averageSoundValue, 1000);
		
		Log.d("MainService", "onCreate ended");
		
		Toast.makeText(this, "Interruption manager service started", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDestroy() {
		handler.removeCallbacks(averageSoundValue);
		mSensorManager.unregisterListener(listener);
		averageSoundValue.interrupt();
		mRecorder.stop();
		mRecorder.release();
		
		Toast.makeText(this, "Interruption manager service stopped", Toast.LENGTH_LONG).show();
		Log.d("LOG", "onDestroy");
	}
	
	@Override
	public void onStart(Intent i, int startid) {
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		mActivity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	private void handleInterruption(String notificationType, String interrupterID) {
		Log.d("LOG", "handleInterruption started");
		this.interrupterID = interrupterID;
		this.notificationType = notificationType;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.situation = prefs.getString("pref_situation", "0");


		Log.d("TRACE", "Starting analysis.");
		aModel.setSituation(this.situation);
		aModel.setInterrupter(this.interrupterID);
		aModel.setNotification(this.notificationType);
		aModel.setSettings(audio.getStreamVolume(AudioManager.STREAM_RING), audio.getRingerMode());
		this.problemState = aModel.detectProblemState(notificationType, interrupterID);
		Resources res = getResources();
		String[] sits = res.getStringArray(R.array.situationArray);
		Log.d("TRACE", "Ended analysis, problem state is: "+sits[Integer.parseInt(this.situation)]);
		
		if (problemState == AnalysisModel.UNWANTED_INTERRUPTION || problemState == AnalysisModel.MISSING_INTERRUPTION) {
			Log.d("TRACE", "Oh noes, a problem!! Starting support.");
			sModel.setProblemState(problemState);
			sModel.setPIV(aModel.getPIV());
			
			performAction(sModel.getAction());
		} else {
			Log.d("TRACE", "No problem state has been detected, just relaxing.");
		}
		Log.d("LOG", "handleInterruption ended");
	}

	private void performAction(ActionObject action) {
		Log.d("TRACE", "Performing actions receieved from support model.");
		setSoundSettings(action.getSoundAction());
		setVibrationSetting(action.getVibrateAction());
		if (action.getSendSMS() == 1) sendSMS(interrupterID, "User is not availeble right now, he will call you back.");

		Intent intent = new Intent("com.example.interruptionmanager.ADAPTATIONACTIVITY");
		intent.putExtra("situation", this.situation)
				.putExtra("interrupter", interrupterID)
				.putExtra("notification", this.notificationType)
				.putExtra("problemState", this.problemState);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		createNotification("Performed action", "Text", 0, pIntent);
	}
	
	private void setSoundSettings(int level) {
//		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING); // current volume
//		audio.setRingerMode(AudioManager.RINGER_MODE_SILENT); // Silent
//		audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); // Vibrate
		Log.d("LOG", "Sound adaptation: " + String.valueOf(level));

		if (level == 0 && audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
			audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			Log.d("TRACE", "Set ringer mode to silent.");
		} else {
			audio.setStreamVolume(AudioManager.STREAM_RING, level, 0);
			Log.d("TRACE", "Set ringer volume to "+String.valueOf(level));
		}
	}
	
	private void setVibrationSetting(int level) {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		Log.d("LOG", "Vibration adaptation: " + String.valueOf(level));
		switch (level) {
		case 0:
			if (audio.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
				audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				Log.d("TRACE", "Set ringer mode to silent.");
			}
			break;
		case 1:
			if (audio.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
				audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); // Vibrate
				Log.d("TRACE", "Set ringer mode to vibrate.");
			}
			break;
		case 2:
			if (audio.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
				audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); // Vibrate
				long[] pattern1 = {500, 500};
				v.vibrate(pattern1, -1);
				Log.d("TRACE", "Set simple vibration pattern.");
			}
		case 3:
			if (audio.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
				audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); // Vibrate
				long[] pattern2 = {3000};
				v.vibrate(pattern2, -1);
				Log.d("TRACE", "Set long vibration pattern.");
			}

		default:
			break;
		}
	}

	private void createNotification(String contentTitle, String contentText, int id, PendingIntent pIntent) {
		// Create notification
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(contentTitle)
		        .setContentText(contentText)
		        .setAutoCancel(true);
		
		mBuilder.setContentIntent(pIntent);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(id, mBuilder.build());
		Log.d("TRACE", "Created notification.");
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
		Log.d("TRACE", "Sent sms to interrupter.");        
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

	private void createData() {
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
		interrupters.add(new Interrupter("+31622725339", 1, 2, 7));
		interrupters.add(new Interrupter("+31659059324", 1, 0, 0));
        
		notifications = new ArrayList<NotificationType>();
		notifications.add(new NotificationType("call", 1, 6, 8));
		notifications.add(new NotificationType("sms", 1, 2, 5));

		aModel.setData(situations, interrupters, notifications);
		sModel.setData(situations, interrupters, notifications);
	}
}
