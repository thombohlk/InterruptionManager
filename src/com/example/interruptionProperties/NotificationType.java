package com.example.interruptionProperties;

public class NotificationType {
	
	public String id;
	public double weight = 0;
	public double cost = 0;
	public double benefit = 0;
	
	
	public NotificationType(String id, double weight, double cost, double benefit) {
		this.id = id;
		this.weight = weight;
		this.cost = cost;
		this.benefit = benefit;
	}
}
