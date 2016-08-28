package com.ibm.iot.motor;

public class MotorException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6248235817383295108L;

	public MotorException(String message, Throwable t) {
		super(message, t);
	}
	
	public MotorException(String message) {
		super(message);
	}
}
