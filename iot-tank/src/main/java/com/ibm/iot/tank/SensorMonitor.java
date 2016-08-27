package com.ibm.iot.tank;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;

public class SensorMonitor {

    private GpioController gpio = null;
    private GpioPinDigitalInput pin = null;

    public SensorMonitor(final DeviceClient device) {
        super();
       
        System.out.println("<--Pi4J--> GPIO Listen Example ... started.");

        gpio = GpioFactory.getInstance();

        pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);
        pin.setShutdownOptions(true);

        pin.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
               
                //Generate a JSON object of the event to be published
                JsonObject jsonEvent = new JsonObject();
                jsonEvent.addProperty("sensor", "right");

                device.publishEvent("collisionDetected", jsonEvent, 0); 
            }
        });
    }

}
