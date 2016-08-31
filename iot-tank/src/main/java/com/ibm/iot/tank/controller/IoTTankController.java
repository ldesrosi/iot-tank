package com.ibm.iot.tank.controller;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

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

	BlockingQueue<Event> eventQueue = null;
	Thread iotEventProducer = null;

	public IoTTankController(Tank tank) {
		this.tank = tank;
		eventQueue = new LinkedTransferQueue<Event>();
		
		iotEventProducer = new Thread(new Runnable() {		
			@Override
			public void run() {
				while (true) {
					try {
						Event event = eventQueue.take();
						IoTManager.getManager().sendEvent(event.topic, event.data);
					} catch (InterruptedException | IoTException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		});
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
				eventQueue.add(new Event("tankDistance", jsonEvent));
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
		try {
			
			JsonObject jsonEvent = new JsonObject();
			jsonEvent.addProperty("sessionId", sessionId);
			jsonEvent.addProperty("turn", side);
	
			System.out.println("Before sending turnComplete");
			eventQueue.add(new Event("turnComplete", jsonEvent));
			System.out.println("After sending turnComplete");
			System.out.println("Out of processTurnComplete");
		} finally {
			turning = false;
		}
	}

	/**
	 * Commands are received from IoT. No-op for this method
	 */
	public void run() {
		iotEventProducer.start();
		
		long id = System.currentTimeMillis();
		
		JsonObject jsonEvent = new JsonObject();
		jsonEvent.addProperty("sessionId", id);

		eventQueue.add(new Event("sessionStarted", jsonEvent));
		sessionId = id;
	}

}

class Event {
	public Event(String topic, JsonObject data) {
		this.topic = topic;
		this.data = data;
	}
	public String topic;
	public JsonObject data;
}
