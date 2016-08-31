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
	private IoTManager distanceIoT = null;
	private IoTManager commandIoT = null;
	private IoTManager eventIoT = null;

	public IoTTankController(Tank tank) {
		this.tank = tank;
		distanceIoT = new IoTManager();
		eventIoT = new IoTManager();
		commandIoT = new IoTManager();
	}
	
	public void init() {
		try {
			distanceIoT.init(false);
			eventIoT.init(false);
			commandIoT.init(true);
			commandIoT.addListener(this);

		} catch (IoTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	/**
	 * Notification received at every distance update.
	 */
	@Override
	public void onDistanceChange(RangeEvent event) {
		System.out.println("Distance=" + event.getDistance());
		
		if (sessionId != -1 && !turning) {
			System.out.println("Getting ready to send distance");
			JsonObject jsonEvent = new JsonObject();
			jsonEvent.addProperty("sessionId", sessionId);
			jsonEvent.addProperty("lastDistance", event.getLastDistance());
			jsonEvent.addProperty("lastTime", event.getLastEventTime());
			jsonEvent.addProperty("distance", event.getDistance());
			jsonEvent.addProperty("eventTime", event.getEventTime());
	
			try {
				System.out.println("Before distanceChange event");
				distanceIoT.sendEvent("tankDistance", jsonEvent);
				System.out.println("After distanceChange event");
				Thread.sleep(500);

			} catch (InterruptedException e) {
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
		//We do not process other commands until the turn is complete.  Discard message.
		if (turning) return;
		
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
		System.out.println("Turn is complete, sending event");
		turning = false;
		
		JsonObject jsonEvent = new JsonObject();
		jsonEvent.addProperty("sessionId", sessionId);
		jsonEvent.addProperty("turn", side);

		System.out.println("Before sending turnComplete");
		eventIoT.sendEvent("turnComplete", jsonEvent);
		System.out.println("After sending turnComplete");
		System.out.println("Out of processTurnComplete");
	}

	/**
	 * Commands are received from IoT. No-op for this method
	 */
	public void run() {
		long id = System.currentTimeMillis();
		
		JsonObject jsonEvent = new JsonObject();
		jsonEvent.addProperty("sessionId", id);

		eventIoT.sendEvent("sessionStarted", jsonEvent);
		sessionId = id;
	}

}
