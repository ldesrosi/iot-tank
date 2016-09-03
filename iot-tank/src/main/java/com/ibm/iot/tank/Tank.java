package com.ibm.iot.tank;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.ibm.iot.motor.DCMotor;
import com.ibm.iot.motor.Motor;
import com.ibm.iot.motor.MotorCommandEnum;
import com.ibm.iot.motor.MotorException;

public class Tank {	
	public static int LEFT_MOTOR = 1;
	public static int RIGHT_MOTOR = 3;
	private static long TURN_WAIT = 1000;
	private static int SPEED = 100;
	
	private DCMotor leftMotor = null;
	private DCMotor rightMotor = null;
	
	private List<DirectionListener> listeners = new LinkedList<DirectionListener>();
	
	//This is assumed heading and will be adjusted relative to the direction the tank is turning to.
	private Heading heading = Heading.NORTH;
	
	public void init() throws MotorException {
		leftMotor = new DCMotor(0x60, 1600, LEFT_MOTOR);
		rightMotor = new DCMotor(0x60, 1600, RIGHT_MOTOR);
	}
	
	public void addListener(DirectionListener listener) {
		assert(listener != null);
		listeners.add(listener);
	}
	
	public void setSpeed(int speed) throws MotorException {
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
	}
	
	public void forward() throws MotorException {
		leftMotor.run(MotorCommandEnum.FORWARD);
		rightMotor.run(MotorCommandEnum.FORWARD);
		setSpeed(SPEED);
	}
	
	public void backward() throws MotorException {
		leftMotor.run(MotorCommandEnum.BACKWARD);
		rightMotor.run(MotorCommandEnum.BACKWARD);
		setSpeed(SPEED);
	}
	
	public void stop() throws MotorException {
		setSpeed(0);
		leftMotor.run(MotorCommandEnum.RELEASE);
		rightMotor.run(MotorCommandEnum.RELEASE);
	}
	
	public void left() throws MotorException {
		leftMotor.run(MotorCommandEnum.BACKWARD);
		rightMotor.run(MotorCommandEnum.FORWARD);
		Motor.delay(TURN_WAIT);
		dispatchEvent(Direction.LEFT);
	}
	
	public void right() throws MotorException {
		rightMotor.run(MotorCommandEnum.BACKWARD);
		leftMotor.run(MotorCommandEnum.FORWARD);
		Motor.delay(TURN_WAIT);
		dispatchEvent(Direction.RIGHT);
	}
	
	private void dispatchEvent(Direction direction) {
		DirectionEvent event = new DirectionEvent();
		event.timestamp = new Date();
		event.direction = direction;
		event.heading = computeNewHeading(direction);
		
		listeners.forEach(listener->{			
			listener.onDirectionChange(event);
		});
	}

	private Heading computeNewHeading(Direction direction) {
		switch(heading) {
			case NORTH:
				heading = (direction == Direction.LEFT)?Heading.WEST:Heading.EAST;
				break;
			case SOUTH:
				heading = (direction == Direction.LEFT)?Heading.EAST:Heading.WEST;
				break;
			case EAST:
				heading = (direction == Direction.LEFT)?Heading.NORTH:Heading.SOUTH;
				break;
			case WEST:
				heading = (direction == Direction.LEFT)?Heading.SOUTH:Heading.NORTH;
				break;
		}
		return heading;
	}
}
