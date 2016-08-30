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
	private boolean turning = false;

	public IoTTankController(Tank tank) {
		this.tank = tank;
	}

	/**
	 * Notification received at every distance update.
	 */
	@Override
	public void onDistanceChange(RangeEvent event) {
		System.out.println("Session ID=" + sessionId + ", turning=" + turning);
		
		if (sessionId != -1 && !turning) {

			System.out.println("Sending a distance update");
			
			JsonObject jsonEvent = new JsonObject();
			jsonEvent.addProperty("sessionId", sessionId);
			jsonEvent.addProperty("lastDistance", event.getLastDistance());
			jsonEvent.addProperty("lastTime", event.getLastEventTime());
			jsonEvent.addProperty("distance", event.getDistance());
			jsonEvent.addProperty("eventTime", event.getEventTime());
	
			try {
				IoTManager.getManager().sendEvent("tankDistance", jsonEvent);
			} catch (IoTException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Process IoT Command - No-op since the basic controller is not connected
	 * to IoT.
	 */
	@Override
	public void processCommand(String command, JsonObject payload) {
		payload = payload.getAsJsonObject("d");
		System.out.println("Received command " + command + " with data " + payload.toString());
		
		//We should always be processing the same session id
		assert(sessionId == payload.getAsJsonPrimitive("sessionId").getAsLong());
				
		try {
			if (payload.has("speed")) {
				tank.setSpeed(payload.getAsJsonPrimitive("speed").getAsInt());
			}
			
			switch (command) {
			case "moveForward":
				tank.forward();
				break;
			case "moveBackward":
				tank.backward();
				break;
			case "turnLeft":
				turning = true;
				tank.left();
				break;
			case "turnRight":
				turning = true;
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
		turning = false;
		
		JsonObject jsonEvent = new JsonObject();
		jsonEvent.addProperty("sessionId", sessionId);
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
		long id = System.currentTimeMillis();
		
		JsonObject jsonEvent = new JsonObject();
		jsonEvent.addProperty("sessionId", id);

		try {
			IoTManager.getManager().sendEvent("sessionStarted", jsonEvent);
		} catch (IoTException e) {
			e.printStackTrace();
		}
		sessionId = id;
	}

}
