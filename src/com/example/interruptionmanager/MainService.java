package com.example.interruptionmanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class MainService extends Service {

	AudioManager audio;
    
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		if(action.equals("android.provider.Telephony.SMS_RECEIVED")){
				Bundle extras = intent.getExtras();
    			createNotification("Received SMS", "You received an SMS from "+extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)+". That's all.", 0);
			}
    		if(action.equals("android.intent.action.PHONE_STATE")){
				Bundle extras = intent.getExtras();
				if (extras != null) {
					String state = extras.getString(TelephonyManager.EXTRA_STATE);
					Log.d("LOG", state);
					if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
						setVibrationSetting(1);
						//sendSMS(extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER), "Thomas is in a meeting right now. He will call you back today.");
					}
				}
			}     
    	}
    };
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		// Set ringer volume
		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		setupCallListener();
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d("LOG", "onDestroy");
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onStart(Intent i, int startid) {
		
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
	}
	
	private void setVibrationSetting(int level) {
		// TODO: make mapping from level to actual settings.
		switch (level) {
		case 1:
			audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); // Vibrate
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

	private void createNotification(String contentTitle, String contentText, int id) {
		// Create notification
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(contentTitle)
		        .setContentText(contentText);
		
		Intent intent = new Intent(this, SettingsActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		mBuilder.setContentIntent(pIntent);
		
		
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(id, mBuilder.build());
	}
	
    private void sendSMS(String phoneNumber, String message)
    {        
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
}
