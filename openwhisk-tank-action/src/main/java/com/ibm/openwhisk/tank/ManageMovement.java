package com.ibm.openwhisk.tank;

import com.google.gson.JsonObject;

public class ManageMovement {
    public static JsonObject main(JsonObject input) {
    	JsonObject tankCommand = new JsonObject();
    	
    	String deviceId = input.getAsJsonPrimitive("deviceId").getAsString();
    	long sessionId =  input.getAsJsonPrimitive("sessionId").getAsLong();
    	tankCommand.addProperty("deviceId", deviceId);
    	tankCommand.addProperty("sessionId", sessionId);

    	tankCommand.addProperty("command", "moveForward");    
    	tankCommand.addProperty("speed",100);
    	
    	// For persistence to Cloudant
    	tankCommand.addProperty("_id", deviceId + "-" + sessionId);
    	tankCommand.addProperty("doc", tankCommand.toString());

    	return tankCommand;
    }
}