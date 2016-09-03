package com.ibm.iot.tank;

public class TankException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6242049551920767346L;

	public TankException(String string, Exception e) {
		super(string, e);
	}

}
