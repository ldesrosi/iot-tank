package com.ibm.iot.tank;

import com.ibm.iot.tank.controller.BasicTankController;
import com.ibm.iot.tank.controller.TankController;

public class App {
	public static void main(String[] args) {
		try {
			Tank tank = new Tank();
			TankController controller = new BasicTankController(tank);
			
			tank.init(controller);
			controller.run();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}
}
