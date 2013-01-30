package com.example.interruptionmanager;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	Button btnSettings;
	Button btnSensors;
	ToggleButton btnOnOff;
	TextView txtStatus;
	
	MediaRecorder mRecorder;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		Log.d("MainActivity", "onCreate started");
        
        StrictMode.enableDefaults();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSettings = (Button)findViewById(R.id.btnPreferences);
        btnSensors = (Button)findViewById(R.id.btnSensors);
        btnOnOff = (ToggleButton)findViewById(R.id.btnOnOff);
        txtStatus = (TextView)findViewById(R.id.txtStatus);

		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Log.d("LOG", prefs.getString("pref_situation", "none"));
		
        btnSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent("com.example.interruptionmanager.SETTINGSACTIVITY");
				startActivity(intent);
			}
		});
        
        btnSensors.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent("com.example.interruptionmanager.SENSORACTIVITY");
				startActivity(intent);
			}
		});
        
        btnOnOff.setActivated(isMainServiceRunning());
        btnOnOff.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent serviceIntent = new Intent(MainActivity.this, MainService.class);
				if (btnOnOff.isChecked()) {
					startService(serviceIntent);
				} else {
					stopService(serviceIntent);
				}
			}
		});
		Log.d("MainActivity", "onCreate ended");
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
        btnOnOff.setChecked(isMainServiceRunning());
    	super.onResume();
    }
    
    private boolean isMainServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MainService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
