package com.ibm.iot.tank.controller;

import com.google.gson.JsonObject;
import com.ibm.iot.comms.IoTException;
import com.ibm.iot.comms.IoTManager;
import com.ibm.iot.motor.MotorException;
import com.ibm.iot.sensor.RangeEvent;
import com.ibm.iot.tank.Tank;

public class IoTTankController implements TankController {
	private Tank tank = null;
	private long sessionId = -1;

	public IoTTankController(Tank tank) {
		this.tank = tank;
	}

	/**
	 * Notification received at every distance update.
	 */
	@Override
	public void onDistanceChange(RangeEvent event) {
		JsonObject jsonEvent = new JsonObject();
		jsonEvent.addProperty("lastDistance", event.getLastDistance());
		jsonEvent.addProperty("lastTime", event.getLastEventTime());
		jsonEvent.addProperty("distance", event.getDistance());
		jsonEvent.addProperty("eventTime", event.getEventTime());

		try {
			IoTManager.getManager().sendEvent("distanceUpdate", jsonEvent);
		} catch (IoTException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process IoT Command - No-op since the basic controller is not connected
	 * to IoT.
	 */
	@Override
	public void processCommand(String command, JsonObject payload) {
		//We should always be processing the same session id
		assert(sessionId == payload.getAsJsonPrimitive("sessionId").getAsLong());
		try {
			switch (command) {
			case "moveForward":
				tank.forward();
				break;
			case "moveBackward":
				tank.backward();
				break;
			case "turnLeft":
				tank.left();
				break;
			case "turnRight":
				tank.right();
				break;
			case "stop":
				tank.stop();
				break;
			default:
				System.out.println("Command '" + command + "' was not recognized");
				return;
			}
		} catch(MotorException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Tank systems notification when a turn is completed
	 */
	@Override
	public void processTurnComplete(String side) {
		JsonObject jsonEvent = new JsonObject();
		jsonEvent.addProperty("turn", side);

		try {
			IoTManager.getManager().sendEvent("turnComplete", jsonEvent);
		} catch (IoTException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Commands are received from IoT. No-op for this method
	 */
	public void run() {
		sessionId = System.currentTimeMillis();
		
		JsonObject jsonEvent = new JsonObject();
		jsonEvent.addProperty("sessionId", sessionId);

		try {
			IoTManager.getManager().sendEvent("sessionStarted", jsonEvent);
		} catch (IoTException e) {
			e.printStackTrace();
		}
	}

}
