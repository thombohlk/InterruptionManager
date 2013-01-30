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
			if (situations.get(i).id.equals(situation)) {
				result += (situations.get(i).benefit - situations.get(i).cost) * situations.get(i).weight;
				Log.d("LOG", "true");
			}
		}
		for (int i = 0; i < interrupters.size(); i++) {
			if (interrupters.get(i).id.equals(interrupter)) result += (interrupters.get(i).benefit - interrupters.get(i).cost) * interrupters.get(i).weight;
		}
		for (int i = 0; i < notifications.size(); i++) {
			if (notifications.get(i).id.equals(notification)) result += (notifications.get(i).benefit - notifications.get(i).cost) * notifications.get(i).weight;
		}
		Log.d("LOG", "piv = " + String.valueOf(result));
		
		return result;
	}

	private Boolean userDetectsInput() {
		// TODO Auto-generated method stub
		if (ringerMode == AudioManager.RINGER_MODE_VIBRATE && lastAveActivity < 3) {
			return true;
		} else if (ringerMode == AudioManager.RINGER_MODE_NORMAL && streamVolume > lastAveSound) {
			return true;
		}
		return false;
	}

	public double getPIV() {
		return piv;
	}

	public void setSituation(String s) {
		this.situation  = s;
	}

	public void setData(ArrayList<Situation> situations, ArrayList<Interrupter> interrupters, ArrayList<NotificationType> notifications) {
		this.situations = situations;
		this.interrupters = interrupters;
		this.notifications = notifications;
	}

	public void setSettings(int streamVolume, int ringerMode) {
		// TODO Auto-generated method stub
		this.streamVolume = streamVolume;
		this.ringerMode = ringerMode;
	}
}
