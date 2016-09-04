package com.ibm.iot.tank.main;

import com.ibm.iot.tank.Tank;
import com.ibm.iot.tank.controller.IoTTankController;
import com.ibm.iot.tank.controller.TankController;

public class App {
	public static void main(String[] args) {
		final Tank tank = new Tank();
		final TankController controller = new IoTTankController(tank);
					
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {			
			@Override
			public void run() {
				try {
					controller.deactivate();
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
		}));
		
		try {
			controller.init();
			controller.activate();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
