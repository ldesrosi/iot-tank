package com.ibm.iot.tank.controller;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import com.google.gson.JsonObject;
import com.ibm.iot.comms.IoTException;
import com.ibm.iot.comms.IoTManager;
import com.ibm.iot.motor.MotorException;
import com.ibm.iot.sensor.RangeEvent;
import com.ibm.iot.tank.Tank;

class Event {
	public Event(String topic, JsonObject data) {
		this.topic = topic;
		this.data = data;
	}
	public String topic;
	public JsonObject data;
}

public class IoTTankController implements TankController {
	private Tank tank = null;
	private long sessionId = -1;
	private boolean turning = false;
	private RangeEvent lastSentEvent = null;

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
		if (sessionId != -1 && !turning) {
			if (lastSentEvent == null) {
				// If this is the first distance event we send it
				System.out.println("Sending first range report");
				sendRangeEvent(event);
			} else if (event.getDistance() < 10 && lastSentEvent.getDistance() >= 10) {
				// If the tank is less then 10 cm away from collision 
				// and this is the first event reporting it we send it
				System.out.println("Sending close range report");
				sendRangeEvent(event);
			} else if ((lastSentEvent.getDistance() - event.getDistance()) > 10) {
				System.out.println("Sending range update report");
				// If the distance between the point where we last sent the event
				// and this event is more then 10 cm we need to update so we sent the event.
				sendRangeEvent(event);
			}
			//Else we ignore the range event...
		}
	}

	private void sendRangeEvent(RangeEvent event) {
		lastSentEvent = event;
		
		JsonObject jsonEvent = new JsonObject();
		jsonEvent.addProperty("sessionId", sessionId);
		jsonEvent.addProperty("lastDistance", event.getLastDistance());
		jsonEvent.addProperty("lastTime", event.getLastEventTime());
		jsonEvent.addProperty("distance", event.getDistance());
		jsonEvent.addProperty("eventTime", event.getEventTime());

		eventQueue.add(new Event("tankDistance", jsonEvent));
	}

	/**
	 * Process IoT Command - No-op since the basic controller is not connected
	 * to IoT.
	 */
	@Override
	public void processCommand(String command, JsonObject payload) {
		//We do not process other commands until the turn is complete.  Discard message.
		if (turning) return;
		
		//Remove the 'd'evice wrapper
		payload = payload.getAsJsonObject("d");
		
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
		try {
			
			JsonObject jsonEvent = new JsonObject();
			jsonEvent.addProperty("sessionId", sessionId);
			jsonEvent.addProperty("turn", side);
	
			eventQueue.add(new Event("turnComplete", jsonEvent));
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
