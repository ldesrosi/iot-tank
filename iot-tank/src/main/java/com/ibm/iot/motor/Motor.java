package com.ibm.iot.motor;

public class Motor {
	private PWM pwm = null;

	protected Motor(int address, int frequency) throws MotorException {
		pwm = new PWM(address, frequency, false);
	}

	protected void setPin(int pin, int value) throws MotorException {
		if (pin < 0 || pin > 15) {
			throw new MotorException(
					"PWM pin must be between 0 and 15 inclusive");
		}

		if (value != 0 && value != 1) {
			throw new MotorException("Pin value must be 0 or 1!");
		}

		if (value == 0) {
			pwm.setPWM(pin, 0, 4096);
		} else {
			pwm.setPWM(pin, 4096, 0);
		}
	}

	protected void setPWM(int pin, int on, int off) throws MotorException {
		pwm.setPWM(pin, on, off);

	}
}
