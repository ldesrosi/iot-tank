package com.ibm.iot.tank.controller;

import com.google.gson.JsonObject;
import com.ibm.iot.motor.MotorException;
import com.ibm.iot.sensor.RangeEvent;
import com.ibm.iot.tank.Tank;

public class BasicTankController implements TankController {
	private Tank tank = null;
	private boolean turnLeft = false;
	
    public BasicTankController(Tank tank) {
		this.tank = tank;
	}
    

	/**
	 * Notification received at every distance update.
	 */
	@Override
	public void onDistanceChange(RangeEvent event) {
		if (event.getDistance() < 5) {
			System.out.println("Collision detected.  Changing course.");
			turnLeft = !turnLeft;
			try {			
				if (turnLeft)
					tank.left();
				else
					tank.right();
			} catch (MotorException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Process IoT Command - No-op since the basic controller is not connected to IoT.
	 */
	@Override
	public void processCommand(String command, JsonObject payload) {
		// No-op
	}

	/**
	 * Tank systems notification when a turn is completed
	 */
	@Override
	public void processTurnComplete(String side) {
		try {
			tank.forward();
		} catch (MotorException e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * Basic event loop to control tank.  Can be running on a separate thread.
	 */
	public void run() {
		try {
			tank.forward();
		} catch (MotorException e) {
			e.printStackTrace();
		}		
	}

}
