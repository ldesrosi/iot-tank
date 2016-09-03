package com.ibm.iot.tank.controller;

import java.text.DateFormat;

import com.google.gson.JsonObject;
import com.ibm.iot.camera.TankVision;
import com.ibm.iot.camera.VisionException;
import com.ibm.iot.comms.CommandListener;
import com.ibm.iot.comms.IoTException;
import com.ibm.iot.comms.IoTManager;
import com.ibm.iot.motor.MotorException;
import com.ibm.iot.sensor.RangeEvent;
import com.ibm.iot.sensor.RangeSensor;
import com.ibm.iot.tank.DirectionEvent;
import com.ibm.iot.tank.Tank;
import com.ibm.iot.tank.TankException;

public class IoTTankController implements TankController, CommandListener {
	private Tank tank = null;
	private RangeSensor rangeSensor = null;
	private TankVision tankVision = null;
	private IoTManager iotManager = null;
	
	private long sessionId = -1;
	private boolean turning = false;
	private RangeEvent lastSentEvent = null;

	public IoTTankController(Tank tank) {
		this.tank = tank;
	}
	
	public void init() throws TankException {
		try {
			this.tank.init();
		} catch (MotorException e) {
			throw new TankException("Error initializing Tank", e);
		}
		this.tank.addListener(this);
		
		this.rangeSensor = new RangeSensor();
		this.rangeSensor.addListener(this);
		
		this.tankVision = new TankVision();
		try {
			this.tankVision.init();
		} catch (VisionException e) {
			throw new TankException("Error initializing TankVision", e);
		}
		
		try {
			this.iotManager = IoTManager.getManager();
		} catch (IoTException e) {
			throw new TankException("Error initializing IoTManager", e);
		}
		this.iotManager.addListener(this);		
	}
	
	public void activate() {
		this.iotManager.activate();	
	}

	public void deactivate() {
		this.iotManager.deactivate();
		this.rangeSensor.deactivate();
		this.tankVision.deactivate();		
	}
	
	@Override
	public void onDirectionChange(DirectionEvent event) {
		JsonObject jsonEvent = new JsonObject();
		jsonEvent.addProperty("sessionId", sessionId);
		jsonEvent.addProperty("timestamp", DateFormat.getDateTimeInstance().format(event.timestamp));
		jsonEvent.addProperty("direction", event.direction.name());
		jsonEvent.addProperty("heading", event.heading.name());
		
		iotManager.sendEvent("directionChange", jsonEvent);

		lastSentEvent = null; //We reset the range event
		turning = false;
	}
	/**
	 * Notification received at every distance update.
	 */
	@Override
	public void onDistanceChange(RangeEvent event) {		
		if (!turning) {
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

		iotManager.sendEvent("tankDistance", jsonEvent);
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
}
