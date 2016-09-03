package com.ibm.iot.tank.Strategy;

public class Step {
	private int id = -1;
	private String heading = null;
	private int speed = 0;
	private int distance = -1;
	private int collisionStep = -1;
	private int nextStep = -1;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getHeading() {
		return heading;
	}
	public void setHeading(String heading) {
		this.heading = heading;
	}
	public int getDistance() {
		return distance;
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}
	public int getCollisionStep() {
		return collisionStep;
	}
	public void setCollisionStep(int collisionStep) {
		this.collisionStep = collisionStep;
	}
	public int getNextStep() {
		return nextStep;
	}
	public void setNextStep(int nextStep) {
		this.nextStep = nextStep;
	}
	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}	
}
