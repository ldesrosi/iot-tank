package com.ibm.iot.tank.controller;

import com.ibm.iot.comms.CommandListener;
import com.ibm.iot.sensor.RangeListener;

public interface TankController extends RangeListener, CommandListener, Runnable {
	public void processTurnComplete(String side);
}
