package com.ibm.iot.tank.controller;

import com.ibm.iot.sensor.RangeListener;
import com.ibm.iot.tank.DirectionListener;

public interface TankController extends RangeListener, DirectionListener {
	public void init() throws Exception;
	
	public void activate();
	public void deactivate();
}
