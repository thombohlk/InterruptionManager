package com.example.interruptionmanager;

import java.util.ArrayList;

import android.util.Log;

import com.example.interruptionProperties.ActionObject;
import com.example.interruptionProperties.Interrupter;
import com.example.interruptionProperties.NotificationType;
import com.example.interruptionProperties.Situation;

public class SupportModel {
	static final int NO_ACTION = 0;
	static final int LOWER_SOUND = 1;
	static final int INCREASE_SOUND = 1;
	
	private double lastAveSound = 0;
	private double lastAveActivity = 0;
	private double lastAveLight = 0;
	
	private int problemState;
	private double piv;

	ArrayList<Situation> situations;
	ArrayList<Interrupter> interrupters;
	ArrayList<NotificationType> notifications;
	
	public SupportModel() {
		
	}
	
	public void updateSensorValues(double sound, double activity, double light) {
		this.lastAveSound = sound;
		this.lastAveActivity = activity;
		this.lastAveLight = light;
	}

	public void setProblemState(int pState) {
		this.problemState = pState;
	}

	public ActionObject getAction() {
		ActionObject result = new ActionObject();
		if (problemState == AnalysisModel.MISSING_INTERRUPTION) {
			result.setSoundAction(determineSoundSetting());
			result.setVibrateAction(determineVibrationSetting());
		}
		if (problemState == AnalysisModel.UNWANTED_INTERRUPTION) {
			result.setSoundAction(0);
			result.setVibrateAction(0);
			result.setSendSMS(1);
		}
		return result;
	}

	private int determineVibrationSetting() {
		Log.d("TRACE", "Reasoning what vibration setting is needed.");
		int result = 0;
		if (lastAveActivity < 1) {
			result = 0;
		} else if (lastAveActivity < 5) {
			result = 1;
		} else {
			result = 2;
		}

		if (piv <= 3) {
			result += 0;
		} else if (piv <= 6) {
			result += 1;
		} else {
			result += 2;
		}
		Log.d("TRACE", "Required vibration setting: "+String.valueOf(Math.min(result, 7)));
		return Math.min(result, 7);
	}

	private int determineSoundSetting() {
		Log.d("TRACE", "Reasoning what sound setting is needed.");
		int result = 0;
		if (lastAveSound < 1) {
			result = 1;
		} else if (lastAveSound < 5) {
			result = 3;
		} else {
			result = 6;
		}

		if (piv <= 3) {
			result += 0;
		} else if (piv <= 6) {
			result += 1;
		} else {
			result += 3;
		}
		Log.d("TRACE", "Required sound setting: "+String.valueOf(Math.min(result, 7)));
		return Math.min(result, 7);
	}

	public void setPIV(double piv) {
		this.piv = piv;
	}

	public void setData(ArrayList<Situation> situations, ArrayList<Interrupter> interrupters, ArrayList<NotificationType> notifications) {
		this.situations = situations;
		this.interrupters = interrupters;
		this.notifications = notifications;
	}
}
