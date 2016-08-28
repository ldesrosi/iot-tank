package com.ibm.iot.motor;

import junit.framework.TestCase;

public class DCMotorTest extends TestCase {

	public void testMotor1() {
		try {
			DCMotor myMotor = new DCMotor(0x60, 1600, 0);
			
			
			System.out.println("Forward! ");
			myMotor.run(MotorCommandEnum.FORWARD);

			System.out.println("\tSpeed up...");
			for (int i = 0; i < 255; i++) {
				myMotor.setSpeed(i);
				Thread.sleep(10);
			}

			System.out.println("\tSlow down...");
			for (int i = 255; i > 0; i--) {
				myMotor.setSpeed(i);
				Thread.sleep(10);
			}

			System.out.println("Backward! ");
			myMotor.run(MotorCommandEnum.FORWARD);

			System.out.println("\tSpeed up...");
			for (int i = 0; i < 255; i++) {
				myMotor.setSpeed(i);
				Thread.sleep(10);
			}

			System.out.println("\tSlow down...");
			for (int i = 255; i > 0; i--) {
				myMotor.setSpeed(i);
				Thread.sleep(10);
			}

			System.out.println("Release");
			myMotor.run(MotorCommandEnum.RELEASE);
			Thread.sleep(1000);
			
		} catch (MotorException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
