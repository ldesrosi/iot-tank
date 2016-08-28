package com.ibm.iot.motor;

public class DCMotor extends Motor {

	private int motornum = -1;
	private int pwmPin;
	private int in1Pin;
	private int in2Pin;

	public DCMotor(int address, int frequency, int num) throws MotorException {
		super(address, frequency);	
		
		motornum = num;

		switch (motornum) {
		case 0:
			pwmPin = 8;
			in2Pin = 9;
			in1Pin = 10;
			break;
		case 1:
			pwmPin = 13;
			in2Pin = 12;
			in1Pin = 11;
			break;
		case 2:
			pwmPin = 2;
			in2Pin = 3;
			in1Pin = 4;
			break;
		case 3:
			pwmPin = 7;
			in2Pin = 6;
			in1Pin = 5;
			break;
		default:
			throw new MotorException(
					"MotorHAT Motor must be between 1 and 4 inclusive");
		}
	}

	public void run(MotorCommandEnum command) throws MotorException {
		switch (command) {
		case FORWARD:
			setPin(in2Pin, 0);
			setPin(in1Pin, 1);
			break;
		case BACKWARD:
			setPin(in1Pin, 0);
			setPin(in2Pin, 1);
			break;
		case RELEASE:
			setPin(in1Pin, 0);
			setPin(in2Pin, 0);
			break;
		}
	}

	public void setSpeed(int speed) throws MotorException {
		speed = Math.max(0, Math.min(255, speed));
		setPWM(pwmPin, 0, speed * 16);
	}
}
