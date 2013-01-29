package com.example.interruptionmanager;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;

public class AdaptationActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        StrictMode.enableDefaults();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adaptation_layout);
    }
	
}
