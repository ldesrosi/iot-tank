package com.ibm.iot.sensor;

import junit.framework.TestCase;

public class RangeSensorTest extends TestCase {
	public void testDistance() {
		RangeSensor sensor = new RangeSensor();
		Thread thread = new Thread(sensor);
		
		thread.start();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		sensor.deactivate();
		
		System.out.println("Final Distance: " + sensor.getDistance());
		
		assertFalse(thread.isAlive());
		assertFalse(sensor.getDistance() == 0);
	}
}
