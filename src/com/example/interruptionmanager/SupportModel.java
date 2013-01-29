package com.example.interruptionmanager;

import java.util.ArrayList;

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
		// TODO Auto-generated method stub
		this.problemState = pState;
	}

	public ActionObject getAction() {
		ActionObject result = new ActionObject();
		if (problemState == AnalysisModel.MISSING_INTERRUPTION) {
			if (piv > 0 && piv <= 3) {
				result.setSoundAction(3);
			} else if (piv <= 6) {
				result.setSoundAction(5);
				result.setVibrateAction(1);
			} else {
				result.setSoundAction(7);
				result.setVibrateAction(2);
			}
		}
		if (problemState == AnalysisModel.UNWANTED_INTERRUPTION) {
			if (piv < 0 && piv >= -3) {
				result.setSoundAction(3);
			} else if (piv >= -6) {
				result.setSoundAction(1);
				result.setVibrateAction(0);
			} else {
				result.setSoundAction(0);
				result.setVibrateAction(0);
				result.setSendSMS(1);
			}
		}
		return result;
	}

	public void setPIV(double piv) {
		// TODO Auto-generated method stub
		this.piv = piv;
	}

	public void setData(ArrayList<Situation> situations,
			ArrayList<Interrupter> interrupters,
			ArrayList<NotificationType> notifications) {
		// TODO Auto-generated method stub
		this.situations = situations;
		this.interrupters = interrupters;
		this.notifications = notifications;
	}
}
