package com.ibm.iot.tank.controller;

import com.ibm.iot.tank.Direction;

public class TankCommand {

	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public Direction getDirection() {
		return direction;
	}
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	private Direction direction;
	private int speed;
}
