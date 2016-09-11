package com.ibm.iot.tank.strategy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.ibm.iot.sensor.RangeEvent;
import com.ibm.iot.tank.Direction;
import com.ibm.iot.tank.controller.TankCommand;

public class TankStrategy {

	private static final double COLLISION_DISTANCE = 7;
	private Step previousStep = null;
	private Step currentStep = null;
	private boolean done = false;
	
	private Map<Integer, Step> stepMap = new HashMap<Integer, Step>();
	
	private static Map<String, Direction> directionMap = new HashMap<String, Direction>();
	
	static {
		directionMap.put("NORTH-NORTH", Direction.FRONT);
		directionMap.put("NORTH-EAST", Direction.RIGHT);
		directionMap.put("NORTH-WEST", Direction.LEFT);
		directionMap.put("NORTH-SOUTH", Direction.TURN_AROUND);
		
		directionMap.put("SOUTH-NORTH", Direction.TURN_AROUND);
		directionMap.put("SOUTH-EAST", Direction.LEFT);
		directionMap.put("SOUTH-WEST", Direction.RIGHT);
		directionMap.put("SOUTH-SOUTH", Direction.FRONT);
		
		directionMap.put("EAST-NORTH", Direction.LEFT);
		directionMap.put("EAST-EAST", Direction.FRONT);
		directionMap.put("EAST-WEST", Direction.TURN_AROUND);
		directionMap.put("EAST-SOUTH", Direction.RIGHT);
		
		directionMap.put("WEST-NORTH", Direction.RIGHT);
		directionMap.put("WEST-EAST", Direction.TURN_AROUND);
		directionMap.put("WEST-WEST", Direction.FRONT);
		directionMap.put("WEST-SOUTH", Direction.LEFT);
	}
	
	public void init(String strategy) {
		System.out.println("About to parse:" + strategy);
		Step[] list = new Gson().fromJson(strategy, Step[].class);

		System.out.println("Strategy has " + list.length + " steps");
		
		previousStep = null;
		if (list.length >= 1)
			currentStep = list[0];
		
		Arrays.stream(list).forEach(step->{
			stepMap.put(new Integer(step.getId()), step);
		});
		

		done = false;
	}
	
	public boolean isDone() {
		return done;
	}
	
	public TankCommand getNextCommand(RangeEvent initialRange, RangeEvent event) {
		//Is this a collision?
		if (event.getDistance() < COLLISION_DISTANCE) {
			System.out.println("Collision distance:" + event.getDistance());
			
			return getNextStep(true);
		} 
		
		//Does this step care about the distance done?
		if (currentStep.getDistance() == -1) {
			System.out.println("Not a direction enabled step");
			return null;
		} else {
			double distanceAchieved = initialRange.getDistance() - event.getDistance();
			
			if (distanceAchieved >= currentStep.getDistance()) {
				System.out.println("Distance achieved:" + distanceAchieved);
				
				return getNextStep(false);
			} else {
				return null;
			}
		}
	}
	
	private TankCommand getNextStep(boolean collision) {
		int nextStepId = (collision)?currentStep.getCollisionStep():currentStep.getNextStep();
		
		//If the id is 0 or smaller we are done
		if (nextStepId <= 0) {
			TankCommand lastCommand = new TankCommand();
			lastCommand.setDirection(Direction.FRONT);
			lastCommand.setSpeed(0);
			
			done = true;
			return lastCommand;
		} else {
			previousStep = currentStep;
			currentStep = stepMap.get(nextStepId);
			return getCurrentCommand();
		}
	}
	
	public TankCommand getCurrentCommand() {
		TankCommand command = new TankCommand();
		
		if (previousStep == null) {
			command.setDirection(Direction.FRONT);
		} else {
			String directionKey = previousStep.getHeading() + "-" + currentStep.getHeading();
			command.setDirection(directionMap.get(directionKey));
		}
		command.setSpeed(currentStep.getSpeed());
		
		return command;
	}
}
