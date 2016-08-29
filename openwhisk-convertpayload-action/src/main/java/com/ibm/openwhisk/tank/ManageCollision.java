package com.ibm.openwhisk.tank;

import com.google.gson.JsonObject;

public class ManageCollision {
    public static JsonObject main(JsonObject input) {
    	double distance = input.getAsJsonPrimitive("distance").getAsDouble();

    	JsonObject tankCommand = new JsonObject();
    	tankCommand.addProperty("deviceId", input.getAsJsonPrimitive("deviceId").getAsString());
    	tankCommand.addProperty("sessionId", input.getAsJsonPrimitive("sessionId").getAsLong());

    	if (distance < 7) {
    		tankCommand.addProperty("command", "turnLeft");    		
    	}

    	return tankCommand;
    }
}
