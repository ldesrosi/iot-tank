package com.ibm.iot.comms;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;

public class IoTManager implements CommandCallback {
	//private static IoTManager manager = null;
	private DeviceClient client = null;
	
	private List<CommandListener> listeners = new LinkedList<CommandListener>();
	
//	public static IoTManager getManager() throws IoTException {
//		if (manager == null) {
//			IoTManager amgr = new IoTManager();
//			amgr.init();
//			
//			manager = amgr;
//		}
//		return manager;
//	}
//	
//	private IoTManager() {
//	}
	
	public void init(boolean listenToEvent) throws IoTException {
	    Properties options = new Properties();

	    try {
			options.load(IoTManager.class.getResourceAsStream("/iotf.properties"));
		} catch (IOException e) {
			throw new IoTException("Error loading configuration file.", e);
		}

    	//Instantiate the class by passing the properties file
    	try {
			client = new DeviceClient(options);
			
			if (listenToEvent) {
				client.setCommandCallback(this);
			}
			
	    	//Connect to the IBM IoT Foundation
	    	client.connect();
		} catch (Exception e) {
			throw new IoTException("Error connecting to the IoT Foundation", e);
		}
	}

	public void sendEvent(String topic, JsonObject event) {
		System.out.println("Before publish " + topic);
		client.publishEvent(topic, event, 0); 
		System.out.println("After publish " + topic);
	}

	public void addListener(CommandListener listener) {
		assert(listener != null);
		
		listeners.add(listener);
	}
	
	@Override
	public void processCommand(Command cmd) {
		System.out.println("Command received:" + cmd.toString());
		JsonParser parser = new JsonParser();
		final JsonObject payload  = parser.parse(cmd.getPayload()).getAsJsonObject();

		listeners.forEach(listener->{			
			listener.processCommand(cmd.getCommand(), payload);
		});
	}
	
	
}

