package com.example.interruptionProperties;

public class Situation {
	
	public String id;
	public double weight = 0;
	public double cost = 0;
	public double benefit = 0;
	
	public Situation(String id, double weight, double cost, double benefit) {
		this.id = id;
		this.weight = weight;
		this.cost = cost;
		this.benefit = benefit;
	}
}
