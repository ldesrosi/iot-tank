package com.ibm.iot.sensor;

import junit.framework.TestCase;

public class RangeSensorTest extends TestCase {
	public void testDistance() {
		RangeSensor sensor = new RangeSensor();
		Thread thread = new Thread(sensor);
		
		thread.start();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		sensor.deactivate();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Final Distance: " + sensor.getDistance());
		
		assertFalse(thread.isAlive());
		assertFalse(sensor.getDistance() == 0);
	}
}
