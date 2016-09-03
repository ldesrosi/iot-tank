package com.ibm.iot.tank.Strategy;

import java.util.ArrayList;
import java.util.List;

public class StepList {
	private List<Step> steps = new ArrayList<Step>();

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}
	
	@Override
	public String toString() {
		return "StepList [steps=" + steps + "]";
	}
}
