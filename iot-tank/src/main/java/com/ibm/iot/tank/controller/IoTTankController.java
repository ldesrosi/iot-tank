package com.ibm.iot.tank.controller;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

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
import com.ibm.iot.tank.strategy.TankStrategy;

public class IoTTankController implements TankController, CommandListener {
	private static final double THRESHOLD = 10;
	private Tank tank = null;
	private RangeSensor rangeSensor = null;
	private TankVision tankVision = null;
	private IoTManager iotManager = null;
	
	private long sessionId = -1;
	
	private DirectionEvent initialDirection = null;
	private RangeEvent initialRange = null;
	private List<RangeEvent> filteredList = new ArrayList<RangeEvent>();

	private RangeEvent lastSentRangeEvent = null;
	private boolean turning = false;
	
	private TankStrategy strategy = null;
	
	public IoTTankController(Tank tank) {
		this.tank = tank;
		this.strategy = new TankStrategy();
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
		System.out.println("Deactivating IoT Tank");
		try {
			tank.stop();
		} catch (MotorException e) {
			System.err.println("Error deactivating tank");
			e.printStackTrace();
		}
		
		this.iotManager.deactivate();
		this.rangeSensor.deactivate();
		this.tankVision.deactivate();		
	}
	
	@Override
	public void onDirectionChange(DirectionEvent event) {
		System.out.println("Direction Change event received");
		String heading = "NORTH";
		
		if (initialDirection == null) {
			initialDirection = event;
		} else {
			heading = initialDirection.heading.name();
		}
		
		double distanceCovered = 0.0;
		if (initialRange != null && lastSentRangeEvent != null) {
			distanceCovered = initialRange.getDistance() - lastSentRangeEvent.getDistance();
		}
		
		JsonObject jsonEvent = new JsonObject();
		jsonEvent.addProperty("type", "direction");
		jsonEvent.addProperty("sessionId", sessionId);
		jsonEvent.addProperty("timestamp", DateFormat.getDateTimeInstance().format(event.timestamp));
		jsonEvent.addProperty("direction", event.direction.name());
		jsonEvent.addProperty("heading", event.heading.name());
		jsonEvent.addProperty("previousHeading", heading);
		jsonEvent.addProperty("distanceCovered", distanceCovered);
				
		iotManager.sendEvent("directionChange", jsonEvent);

		//We go forward now...
		try {
			System.out.println("Going forward now.");
			tank.forward();
		} catch(MotorException e) {
			System.err.println("Error going forward after a turn.");
			e.printStackTrace();
		}
		
		//We reset all state variables
		initialDirection = event;
		initialRange = null;
		lastSentRangeEvent = null; 
		turning = false;
		
	}
	/**
	 * Notification received at every distance update.
	 */
	@Override
	public void onDistanceChange(RangeEvent event) {		
		if (!turning) {
			// We filter out spike in distance...
			double delta = event.getLastDistance() - event.getDistance();
			if (delta > THRESHOLD) {
				System.out.println("Ignoring spike in distance:" + delta);
				return;
			}
			
			if (initialRange == null) {
				initialRange = filterEvent(event);
				return;
			}
			
			//Determining if we send an event to OpenWhisk
			if (lastSentRangeEvent == null) {
				// If this is the first distance event we send it
				System.out.println("Sending first range report");
				sendRangeEvent(event);
			} else {
				double progress = lastSentRangeEvent.getDistance() - event.getDistance();
				if (progress > 5) {
					sendRangeEvent(event);
				}
			}
			
			if (!strategy.isDone()) {
				processStrategyCommand(strategy.getNextCommand(initialRange, event));
			}
		}
	}

	private RangeEvent filterEvent(RangeEvent event) {
		if (filteredList.size() < 5) {
			filteredList.add(event);
			return null;
		} else {
			double average = filteredList.stream().mapToDouble(e->{return e.getDistance(); }).average().orElse(0.0);
			RangeEvent selectedEvent = null;
			for (RangeEvent rangeEvent : filteredList) {
				if (selectedEvent == null) {
					selectedEvent = rangeEvent;
				} else {
					if (Math.abs(rangeEvent.getDistance() - average) < 
							Math.abs(selectedEvent.getDistance() - average)) {
						selectedEvent = rangeEvent;
					}
				}
			}
			filteredList.clear();
			return selectedEvent;
		}
	}

	private void sendRangeEvent(RangeEvent event) {
		lastSentRangeEvent = event;
		
		JsonObject jsonEvent = new JsonObject();
		jsonEvent.addProperty("type", "range");
		jsonEvent.addProperty("sessionId", sessionId);
		jsonEvent.addProperty("timestamp", DateFormat.getDateTimeInstance().format(event.getTimestamp()));
		jsonEvent.addProperty("lastDistance", event.getLastDistance());
		jsonEvent.addProperty("lastEventTime", event.getLastEventTime());
		jsonEvent.addProperty("distance", event.getDistance());
		jsonEvent.addProperty("eventTime", event.getEventTime());

		iotManager.sendEvent("distanceChange", jsonEvent);
	}

	/**
	 * Process IoT Command 
	 */
	@Override
	public void processCommand(String command, JsonObject payload) {		
		//Remove the 'd'evice wrapper
		payload = payload.getAsJsonObject("d");
						
		try {			
			switch (command) {
			case "startSession":
				startSession(payload);
				break;
			case "stopSession":
				stopSession();
				break;
			default:
				System.out.println("Command '" + command + "' was not recognized");
				return;
			}
		} catch(MotorException e) {
			e.printStackTrace();
		}
	}
	
	private void processStrategyCommand(TankCommand command) {
		//Null means no change to direction...
		if (command == null) return;
		if (command.getDirection() == null) return;
		
		System.out.println("Command=" + command.getDirection());
		System.out.println("Speed=" + command.getSpeed());
		try{
			switch(command.getDirection()) {
			case FRONT:
				tank.forward();
				break;
			case LEFT:
				turning = true;
				tank.left();
				break;
			case RIGHT:
				turning = true;
				tank.right();
				break;
			case TURN_AROUND:
				turning = true;
				tank.turnAround();
				break;
			case BACK:
				tank.backward();
				break;
			}
			if (command.getSpeed() == 0) {
				tank.stop();
			} else {
				tank.setSpeed(command.getSpeed());
			}
		} catch(MotorException e) {
			System.err.println("Could not process command; received motor exception");
			e.printStackTrace();
		}
	}

	private void startSession(JsonObject payload) {
		sessionId = payload.get("sessionId").getAsLong();
		
		String strStrategy = payload.get("strategy").getAsString();
		strategy.init(strStrategy);
		
		this.tankVision.setSessionId(sessionId);
		
		this.rangeSensor.activate();
		this.tankVision.activate();
		
		lastSentRangeEvent = null;
		initialDirection = null;
		initialRange = null;
		
		if(!strategy.isDone()) {
			processStrategyCommand(strategy.getCurrentCommand());
		}
	}
	
	private void stopSession() throws MotorException {
		tank.stop();		
		this.rangeSensor.deactivate();
		this.tankVision.deactivate();
		
		sessionId = 0;
		this.tankVision.setSessionId(sessionId);

	}
}
