package com.ibm.iot.tank.controller;

import com.ibm.iot.motor.MotorException;
import com.ibm.iot.sensor.RangeEvent;
import com.ibm.iot.sensor.RangeSensor;
import com.ibm.iot.tank.DirectionEvent;
import com.ibm.iot.tank.Tank;

public class BasicTankController implements TankController {
	private Tank tank = null;
	private RangeSensor rangeSensor = null;

	private boolean turnLeft = false;

	public BasicTankController(Tank tank) {
		this.tank = tank;
	}
	
	public void init() {
		this.tank.addListener(this);
		
		this.rangeSensor = new RangeSensor();
		this.rangeSensor.addListener(this);
	}

	public void activate() {
		rangeSensor.activate();
		
		try {
			tank.forward();
		} catch (MotorException e) {
			e.printStackTrace();
		}
	}
	
	public void deactivate() {
		rangeSensor.deactivate();
		
		try {
			tank.stop();
		} catch (MotorException e) {
			e.printStackTrace();
		}
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

	@Override
	public void onDirectionChange(DirectionEvent event) {
		try {
			tank.forward();
		} catch (MotorException e) {
			e.printStackTrace();
		}
	}
}
