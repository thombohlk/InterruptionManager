package com.example.interruptionmanager;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class MainService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("My notification")
		        .setContentText("Hello World!");
		
		Intent intent = new Intent(this, SettingsActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		mBuilder.setContentIntent(pIntent);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(1, mBuilder.build());
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d("LOG", "onDestroy");
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onStart(Intent i, int startid) {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("My notification")
		        .setContentText("Hello World!")
		        .setOngoing(false)
		        .setAutoCancel(true);
		
		Intent intent = new Intent(this, SettingsActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		mBuilder.setContentIntent(pIntent);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(1, mBuilder.build());
	}
}
