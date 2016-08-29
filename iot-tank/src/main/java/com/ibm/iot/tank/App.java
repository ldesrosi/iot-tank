package com.ibm.iot.tank;

import com.ibm.iot.comms.IoTManager;
import com.ibm.iot.motor.MotorException;
import com.ibm.iot.tank.controller.BasicTankController;
import com.ibm.iot.tank.controller.IoTTankController;
import com.ibm.iot.tank.controller.TankController;

public class App {
	public static void main(String[] args) {
		try {
			//Start by connecting to IoT before starting any other thread.
			IoTManager.getManager();
			
			final Tank tank = new Tank();
			final TankController controller = new IoTTankController(tank);
			
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {			
				@Override
				public void run() {
					try {
						tank.stop();
					} catch (MotorException e) {
						e.printStackTrace();
					}				
				}
			}));
			
			tank.init(controller);
			controller.run();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}
}
