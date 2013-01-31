package com.example.interruptionmanager;

import java.util.ArrayList;

import com.example.interruptionProperties.Interrupter;
import com.example.interruptionProperties.NotificationType;
import com.example.interruptionProperties.Situation;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class AdaptationActivity extends Activity {
	
	static final String INCREASE_SIT = "intent";

	Button btnOk, btnCancel;
	TextView txtAdaptationDescription;
	CheckBox cbxSitCost, cbxSitBenefit, cbxInterCost, cbxInterBenefit, cbxNotiCost, cbxNotiBenefit;

	ArrayList<Situation> situations;
	ArrayList<Interrupter> interrupters;
	ArrayList<NotificationType> notifications;
	private String 	situation;
	private String 	interrupter;
	private String 	notification;
	private int 	problemState;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Bundle extras = getIntent().getExtras();
    	situation = extras.getString("situation");
    	interrupter = extras.getString("interrupter");
    	notification = extras.getString("notification");
    	problemState = extras.getInt("problemState");
    	
        StrictMode.enableDefaults();
                
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adaptation_layout);
        txtAdaptationDescription = (TextView)findViewById(R.id.txtAdaptationDescription);
        btnOk = (Button)findViewById(R.id.confirm);
        btnCancel = (Button)findViewById(R.id.cancel);

        cbxSitBenefit = (CheckBox)findViewById(R.id.cbxSituationBenefit);
        cbxSitCost = (CheckBox)findViewById(R.id.cbxSituationCost);
        cbxInterBenefit = (CheckBox)findViewById(R.id.cbxInterrupterBenefit);
        cbxInterCost = (CheckBox)findViewById(R.id.cbxInterrupterCost);
        cbxNotiBenefit = (CheckBox)findViewById(R.id.cbxNotificationBenefit);
        cbxNotiCost = (CheckBox)findViewById(R.id.cbxNotificationCost);
    	setDescriptionText();
    	
    	Log.d("TRACE", "Asking user for feedback on action.");

        btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				changeValues();
			}
		});

        btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				closeActivity();
			}
		});
    }

	private void changeValues() {
		Intent intent = new Intent(AdaptationActivity.INCREASE_SIT)
		.putExtra("situation", situation)
		.putExtra("interrupter", interrupter)
		.putExtra("notification", notification);
		if (problemState == AnalysisModel.MISSING_INTERRUPTION) {
			if (cbxSitBenefit.isChecked()) intent.putExtra("situationBenefit", -1);
			if (cbxInterBenefit.isChecked()) intent.putExtra("interrupterBenefit", -1);
			if (cbxNotiBenefit.isChecked()) intent.putExtra("notificationBenefit", -1);
			if (cbxSitCost.isChecked()) intent.putExtra("situationCost", 1);
			if (cbxInterCost.isChecked()) intent.putExtra("interrupterCost", 1);
			if (cbxNotiCost.isChecked()) intent.putExtra("notificationCost", 1);
		}
		if (problemState == AnalysisModel.UNWANTED_INTERRUPTION) {
			if (cbxSitBenefit.isChecked()) intent.putExtra("situationBenefit", 1);
			if (cbxInterBenefit.isChecked()) intent.putExtra("interrupterBenefit", 1);
			if (cbxNotiBenefit.isChecked()) intent.putExtra("notificationBenefit", 1);
			if (cbxSitCost.isChecked()) intent.putExtra("situationCost", -1);
			if (cbxInterCost.isChecked()) intent.putExtra("interrupterCost", -1);
			if (cbxNotiCost.isChecked()) intent.putExtra("notificationCost", -1);
		}
		sendBroadcast(new Intent(intent));
		closeActivity();
	}
    
    private void closeActivity() {
		this.finish();
	}

	private void setDescriptionText() {
        if (problemState == AnalysisModel.MISSING_INTERRUPTION) {
        	Resources res = getResources();
        	String[] situations = res.getStringArray(R.array.listArray);
        	Log.d("LOG", situation);
        	Log.d("LOG", String.valueOf(Integer.parseInt(situation)));
        	String sit = situations[Integer.parseInt(situation)];
            String text = "Interruption manager made sure you noticed an incomming "+notification+". This interruption came from "+interrupter+" and occurred during the situation "+sit+".";
            text.concat("If this was not ok, please specify why.");
            txtAdaptationDescription.setText(text);
        	cbxSitBenefit.setText("Situation less beneficial");
        	cbxSitCost.setText("Situation more costly");
        	cbxInterBenefit.setText("Interrupter less beneficial");
        	cbxInterCost.setText("Interrupter more costly");
        	cbxNotiBenefit.setText("Notification less beneficial");
        	cbxNotiCost.setText("Notification more costly");
        }
        if (problemState == AnalysisModel.UNWANTED_INTERRUPTION) {
            String text = "Interruption manager has stopped an incomming "+notification+". This interruption came from "+interrupter+" and occurred during the situation "+situation+".";
            text.concat("If this was not ok, please specify why.");
            txtAdaptationDescription.setText(text);
        	cbxSitBenefit.setText("Situation more beneficial");
        	cbxSitCost.setText("Situation less costly");
        	cbxInterBenefit.setText("Interrupter more beneficial");
        	cbxInterCost.setText("Interrupter less costly");
        	cbxNotiBenefit.setText("Notification more beneficial");
        	cbxNotiCost.setText("Notification less costly");
        }
	}
	
}
