package com.example.interruptionmanager;

import java.util.ArrayList;

import com.example.interruptionProperties.Interrupter;
import com.example.interruptionProperties.NotificationType;
import com.example.interruptionProperties.Situation;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Audio;
import android.util.Log;

public class AnalysisModel {
	static final int NO_PROBLEM = 0;
	static final int UNWANTED_INTERRUPTION = 1;
	static final int MISSING_INTERRUPTION = 2;

	private double lastAveSound = 0;
	private double lastAveActivity = 0;
	private double lastAveLight = 0;
	private double piv = 0;
	private String situation = "0";
	private String interrupter;
	private String notification;
	private int streamVolume = 0;
	private int ringerMode = 0;

	ArrayList<Situation> situations;
	ArrayList<Interrupter> interrupters;
	ArrayList<NotificationType> notifications;
	
	public AnalysisModel() {
		
	}
	
	public void updateSensorValues(double sound, double activity, double light) {
		lastAveSound = sound;
		lastAveActivity = activity;
		lastAveLight = light;
	}

	public int detectProblemState(String notification, String interrupter) {
		boolean detected = userDetectsInput();
		piv = calculatePIV(notification, interrupter);

		Log.d("LOG", "detected = "+ String.valueOf(detected));
		if (detected && piv < 0) {
			Log.d("LOG", "situation = UNWANTED_INTERRUPTION");
			return UNWANTED_INTERRUPTION;
		} else if (!detected && piv > 0) {
			Log.d("LOG", "situation = MISSING_INTERRUPTION");
			return MISSING_INTERRUPTION;
		}
		
		return NO_PROBLEM;
	}

	private double calculatePIV(String notification, String interrupter) {
		double result = 0;

		for (int i = 0; i < situations.size(); i++) {
			if (situations.get(i).id.equals(situation)) result += (situations.get(i).benefit - situations.get(i).cost) * situations.get(i).weight;
		}
		for (int i = 0; i < interrupters.size(); i++) {
			if (interrupters.get(i).id.equals(interrupter)) result += (interrupters.get(i).benefit - interrupters.get(i).cost) * interrupters.get(i).weight;
		}
		for (int i = 0; i < notifications.size(); i++) {
			if (notifications.get(i).id.equals(notification)) result += (notifications.get(i).benefit - notifications.get(i).cost) * notifications.get(i).weight;
		}
		Log.d("TRACE", "Calculated Predicted Interruption Value of " + String.valueOf(result));
		
		return result;
	}

	private Boolean userDetectsInput() {
		Log.d("TRACE", "Reasoning if user will detect incomming interruption.");
		if (ringerMode == AudioManager.RINGER_MODE_VIBRATE && lastAveActivity < 5) {
			Log.d("TRACE", "User will detect incomming interruption.");
			return true;
		} else if (ringerMode == AudioManager.RINGER_MODE_NORMAL && (streamVolume == 1 || streamVolume == 2) && lastAveSound < 1) {
			Log.d("TRACE", "User will detect incomming interruption.");
			return true;
		} else if (ringerMode == AudioManager.RINGER_MODE_NORMAL && (streamVolume == 3 || streamVolume == 4) && lastAveSound < 5) {
			Log.d("TRACE", "User will detect incomming interruption.");
			return true;
		} else if (ringerMode == AudioManager.RINGER_MODE_NORMAL && streamVolume >= 5 && lastAveSound < 9) {
			Log.d("TRACE", "User will detect incomming interruption.");
			return true;
		} else if (lastAveLight > 50 && lastAveActivity > 0.02) {
			Log.d("TRACE", "User will detect incomming interruption.");
			return true;
		}
		Log.d("TRACE", "User will not detect incomming interruption.");
		return false;
	}

	public double getPIV() {
		return piv;
	}

	public void setSituation(String s) {
		this.situation  = s;
	}

	public void setInterrupter(String i) {
		this.interrupter  = i;
	}

	public void setNotification(String n) {
		this.notification  = n;
	}

	public void setData(ArrayList<Situation> situations, ArrayList<Interrupter> interrupters, ArrayList<NotificationType> notifications) {
		this.situations = situations;
		this.interrupters = interrupters;
		this.notifications = notifications;
	}

	public void setSettings(int streamVolume, int ringerMode) {
		this.streamVolume = streamVolume;
		this.ringerMode = ringerMode;
	}
}
