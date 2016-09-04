package com.ibm.iot.comms;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;

class Event {
	public Event(String topic, JsonObject data) {
		this.topic = topic;
		this.data = data;
	}
	public String topic;
	public JsonObject data;
}

public class IoTManager implements CommandCallback, Runnable {
	private static IoTManager manager = null;
	private DeviceClient client = null;
	
	private List<CommandListener> listeners = new LinkedList<CommandListener>();
	private BlockingQueue<Event> eventQueue = null;
	
	private boolean active = true;
	
	private Thread executionThread = null;
	
	private IoTManager() {
	}
	
	public static IoTManager getManager() throws IoTException {
		if (manager == null) {
			IoTManager amgr = new IoTManager();
			amgr.init();
			
			manager = amgr;
		}
		return manager;
	}
		
	public void init() throws IoTException {
		eventQueue = new LinkedTransferQueue<Event>();
		
	    Properties options = new Properties();

	    try {
			options.load(IoTManager.class.getResourceAsStream("/iotf.properties"));
		} catch (IOException e) {
			throw new IoTException("Error loading configuration file.", e);
		}

    	//Instantiate the class by passing the properties file
    	try {
			client = new DeviceClient(options);
			
			client.setCommandCallback(this);
			
	    	//Connect to the IBM IoT Foundation
	    	client.connect();
		} catch (Exception e) {
			throw new IoTException("Error connecting to the IoT Foundation", e);
		}
	}
	
	public void activate() {
		System.out.println("IoTManager activated");
		executionThread = new Thread(this);
		executionThread.start();
	}
	
	public void deactivate() {
		active = false;
	}

	public void addListener(CommandListener listener) {
		assert(listener != null);
		listeners.add(listener);
	}
	
	public void sendEvent(String topic, JsonObject event) {
		eventQueue.add(new Event(topic, event));
	}
	
	@Override
	public void processCommand(Command cmd) {
		JsonParser parser = new JsonParser();
		final JsonObject payload  = parser.parse(cmd.getPayload()).getAsJsonObject();

		listeners.forEach(listener->{			
			listener.processCommand(cmd.getCommand(), payload);
		});
	}

	@Override
	public void run() {
		while (active) {
			Event event = null;
			try {
				event = eventQueue.take();
				
				if (event != null) {
					client.publishEvent(event.topic, event.data);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
	}
}

