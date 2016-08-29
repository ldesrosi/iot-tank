package com.ibm.iot.tank;

import com.ibm.iot.motor.DCMotor;
import com.ibm.iot.motor.Motor;
import com.ibm.iot.motor.MotorCommandEnum;
import com.ibm.iot.motor.MotorException;
import com.ibm.iot.sensor.RangeSensor;
import com.ibm.iot.tank.controller.TankController;

public class Tank {
	public static final String LEFT = "LEFT";
	public static final String RIGHT = "RIGHT";
	
	public static int LEFT_MOTOR = 0;
	public static int RIGHT_MOTOR = 3;
	private static long TURN_WAIT = 1000;
	
	private DCMotor leftMotor = null;
	private DCMotor rightMotor = null;
	
	private RangeSensor rangeSensor = null;
	private Thread rangeSensorThread = null;
	
	private TankController controller = null;
	
	public void init(TankController listener) throws MotorException {
		leftMotor = new DCMotor(0x60, 1600, LEFT_MOTOR);
		rightMotor = new DCMotor(0x60, 1600, RIGHT_MOTOR);
		
		rangeSensor = new RangeSensor();
		rangeSensor.addListener(listener);
		rangeSensorThread = new Thread(rangeSensor);
		rangeSensorThread.start();
		
		controller = listener;
	}
	
	public void forward() throws MotorException {
		leftMotor.run(MotorCommandEnum.FORWARD);
		rightMotor.run(MotorCommandEnum.FORWARD);
	}
	
	public void backward() throws MotorException {
		leftMotor.run(MotorCommandEnum.BACKWARD);
		rightMotor.run(MotorCommandEnum.BACKWARD);
	}
	
	public void stop() throws MotorException {
		leftMotor.run(MotorCommandEnum.RELEASE);
		rightMotor.run(MotorCommandEnum.RELEASE);
	}
	
	public void left() throws MotorException {
		leftMotor.run(MotorCommandEnum.BACKWARD);
		rightMotor.run(MotorCommandEnum.FORWARD);
		Motor.delay(TURN_WAIT);
		controller.processTurnComplete(LEFT);
	}
	
	public void right() throws MotorException {
		rightMotor.run(MotorCommandEnum.BACKWARD);
		leftMotor.run(MotorCommandEnum.FORWARD);
		Motor.delay(TURN_WAIT);
		controller.processTurnComplete(RIGHT);
	}
}
