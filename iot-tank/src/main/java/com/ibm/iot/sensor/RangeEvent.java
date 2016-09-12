package com.ibm.iot.sensor;

import java.util.Date;

public class RangeEvent {
	private Date timestamp = new Date();
	
	private long lastEventTime;
	private double lastDistance;

	private long eventTime;
	private double distance;
	
	public long getLastEventTime() {
		return lastEventTime;
	}
	public void setLastEventTime(long lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
	public double getLastDistance() {
		return lastDistance;
	}
	public void setLastDistance(double lastDistance) {
		this.lastDistance = lastDistance;
	}
	public long getEventTime() {
		return eventTime;
	}
	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

}
