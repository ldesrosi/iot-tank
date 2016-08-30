package com.ibm.openwhisk.tank;

import com.google.gson.JsonObject;

public class ManageDistance {
    public static JsonObject main(JsonObject input) {
    	double distance = input.getAsJsonPrimitive("distance").getAsDouble();

    	JsonObject tankCommand = new JsonObject();
    	tankCommand.addProperty("deviceId", input.getAsJsonPrimitive("deviceId").getAsString());
    	tankCommand.addProperty("sessionId", input.getAsJsonPrimitive("sessionId").getAsLong());

    	if (distance < 5) {
    		tankCommand.addProperty("command", "turnLeft");    		
    	} else if (distance >= 5 && distance < 10) {
        	tankCommand.addProperty("speed", 60);
    	} else {
        	tankCommand.addProperty("speed", 100);
    	}

    	return tankCommand;
    }
}
