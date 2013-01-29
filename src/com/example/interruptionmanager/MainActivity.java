package com.example.interruptionmanager;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.StrictMode;
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
	
	static HttpClient httpclient;
	static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        StrictMode.enableDefaults();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        httpclient = new DefaultHttpClient(); 

        btnSettings = (Button)findViewById(R.id.btnPreferences);
        btnSensors = (Button)findViewById(R.id.btnSensors);
        btnOnOff = (ToggleButton)findViewById(R.id.btnOnOff);
        txtStatus = (TextView)findViewById(R.id.txtStatus);
        
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
