package com.ibm.iot.comms;

public interface CommandListener {
	public void processCommand(String command, String payload);
}
