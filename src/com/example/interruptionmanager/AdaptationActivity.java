package com.example.interruptionmanager;

import java.util.ArrayList;

import com.example.interruptionProperties.Interrupter;
import com.example.interruptionProperties.NotificationType;
import com.example.interruptionProperties.Situation;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

public class AdaptationActivity extends Activity {

	Button btnOk;
	RadioGroup rdgReasonDeviation;
	private int problemState = AnalysisModel.NO_PROBLEM;

	ArrayList<Situation> situations;
	ArrayList<Interrupter> interrupters;
	ArrayList<NotificationType> notifications;
	private String situation;
	private String interrupter;
	private String notification;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Bundle extras = getIntent().getExtras();
    	situation = extras.getString("situation");
    	interrupter = extras.getString("interrupter");
    	notification = extras.getString("notification");
    	
        StrictMode.enableDefaults();
                
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adaptation_layout);
        btnOk = (Button)findViewById(R.id.confirm);
        rdgReasonDeviation = (RadioGroup)findViewById(R.id.rdgReasonDeviation);
        
        btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				changeWeight(rdgReasonDeviation.getCheckedRadioButtonId());
			}
		});
    }

	private void changeWeight(int value) {
		switch (value) {
		case R.id.rdbSituation:
			changeSitWeight();
			Toast.makeText(this, situation, Toast.LENGTH_SHORT).show();
			//closeActivity();
			break;
		case R.id.rdbInterrupter:
			break;
		case R.id.rdbNotification:
			break;
		default:
			Toast.makeText(this, "Please enter a reason.", Toast.LENGTH_SHORT).show();
			break;
		}
	}
    
    private void closeActivity() {
		this.finish();
	}

	private void changeSitWeight() {
		//for (int i = 0; i < situations.size(); i++) {
			//if (situations.get(i) == )
		//}
	}

	public void setProblemState(int problemState) {
    	this.problemState  = problemState;
    }
	
}
