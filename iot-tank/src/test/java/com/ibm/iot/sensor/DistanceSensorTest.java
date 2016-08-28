package com.ibm.iot.sensor;

import junit.framework.TestCase;

public class DistanceSensorTest extends TestCase {
	public void testDistance() {
		RangeSensor sensor = new RangeSensor();
		
		sensor.run();
//		Thread thread = new Thread(sensor);
//		
//		thread.start();
//		
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		sensor.deactivate();
//		
//		System.out.println("Final Distance: " + sensor.getDistance());
	}
}
