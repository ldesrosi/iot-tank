package com.ibm.iot.motor;

import junit.framework.TestCase;

public class DCMotorTest extends TestCase {

	public void testMotor1() {
		try {
			DCMotor motor = new DCMotor(0x60, 1600, 1);
			
			motor.run(MotorCommandEnum.FORWARD);
			Thread.sleep(2000);
			
			motor.run(MotorCommandEnum.BACKWARD);
			Thread.sleep(2000);
			
			motor.run(MotorCommandEnum.RELEASE);
		} catch (MotorException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
