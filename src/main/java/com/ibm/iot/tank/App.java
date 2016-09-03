package com.ibm.iot.tank;

import com.ibm.iot.camera.TankVision;
import com.ibm.iot.comms.IoTManager;
import com.ibm.iot.motor.MotorException;
import com.ibm.iot.tank.controller.IoTTankController;
import com.ibm.iot.tank.controller.TankController;

public class App {
	public static void main(String[] args) {
		try {
			IoTManager manager = IoTManager.getManager();
			
			final Tank tank = new Tank();
			final TankController controller = new IoTTankController(tank);
			
			final TankVision vision = new TankVision();
			vision.init();
			final Thread tankVisionThread = new Thread(vision);
			
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
			
			manager.addListener(controller);
			tank.init(controller);
			controller.run();
			tankVisionThread.run();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}
}
