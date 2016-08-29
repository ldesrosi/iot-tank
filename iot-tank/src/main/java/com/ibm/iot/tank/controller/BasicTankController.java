package com.ibm.iot.tank.controller;

import com.ibm.iot.motor.MotorException;
import com.ibm.iot.tank.Tank;

public class BasicTankController implements TankController {
	private Tank tank = null;
	private boolean turnLeft = false;
	
    public BasicTankController(Tank tank) {
		this.tank = tank;
	}
    
	@Override
	public void onDistanceChange(double lastDistance, double distance) {
		if (distance < 5) {
			turnLeft = !turnLeft;
			try {			
				if (turnLeft)
					tank.left();
				else
					tank.right();
			} catch (MotorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void processCommand(String command, String payload) {
		// No-op

	}

	@Override
	public void processTurnComplete(String side) {
		try {
			tank.forward();
		} catch (MotorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			tank.forward();
		} catch (MotorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
