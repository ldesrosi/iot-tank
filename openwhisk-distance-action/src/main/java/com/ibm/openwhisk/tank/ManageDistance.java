package com.ibm.openwhisk.tank;

import com.google.gson.JsonObject;

public class ManageDistance {
    public static JsonObject main(JsonObject input) {
    	double distance = input.getAsJsonPrimitive("distance").getAsDouble();

    	JsonObject tankCommand = new JsonObject();
    	tankCommand.addProperty("deviceId", input.getAsJsonPrimitive("deviceId").getAsString());
    	tankCommand.addProperty("sessionId", input.getAsJsonPrimitive("sessionId").getAsLong());

    	if (distance < 10) {
    		tankCommand.addProperty("command", "turnLeft");    
    		tankCommand.addProperty("speed", 60);
    	} else if (distance >= 10 && distance < 15) {
    		tankCommand.addProperty("command", "moveForward");
        	tankCommand.addProperty("speed", 60);
    	} else {
    		tankCommand.addProperty("command", "moveForward");
        	tankCommand.addProperty("speed", 100);
    	}

    	return tankCommand;
    }
}
