package com.example.interruptionProperties;

public class ActionObject {

	private int soundAction = -1;
	private int vibrateAction = -1;
	private int lightAction = -1;
	private int sendSMS = -1;
	
	public ActionObject(){
		
	}

	public int getSoundAction() {
		return soundAction;
	}

	public void setSoundAction(int soundAction) {
		this.soundAction = soundAction;
	}

	public int getVibrateAction() {
		return vibrateAction;
	}

	public void setVibrateAction(int vibrateAction) {
		this.vibrateAction = vibrateAction;
	}

	public int getLightAction() {
		return lightAction;
	}

	public void setLightAction(int lightAction) {
		this.lightAction = lightAction;
	}

	public int getSendSMS() {
		return sendSMS;
	}

	public void setSendSMS(int sendSMS) {
		this.sendSMS = sendSMS;
	}
}
