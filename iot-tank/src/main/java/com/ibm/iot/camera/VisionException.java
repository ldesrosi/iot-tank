package com.ibm.iot.camera;


public class VisionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public VisionException(String message, Exception e) {
		super(message, e);
	}

	public VisionException(String message) {
		super(message);
	}

}
