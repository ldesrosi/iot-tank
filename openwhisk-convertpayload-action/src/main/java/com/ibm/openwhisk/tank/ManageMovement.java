package com.ibm.openwhisk.tank;

import com.google.gson.JsonObject;

public class ManageMovement {
    public static JsonObject main(JsonObject input) {
    	JsonObject tankCommand = new JsonObject();
    	tankCommand.addProperty("deviceId", input.getAsJsonPrimitive("deviceId").getAsString());
    	tankCommand.addProperty("sessionId", input.getAsJsonPrimitive("sessionId").getAsLong());

    	tankCommand.addProperty("command", "forward");    
    	tankCommand.addProperty("speed",100);

    	return tankCommand;
    }
}