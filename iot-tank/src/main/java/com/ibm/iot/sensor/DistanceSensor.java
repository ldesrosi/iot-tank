package com.ibm.iot.sensor;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;

public class DistanceSensor implements Runnable {
	private boolean active = true;
	
    private GpioController gpio = null;
    private GpioPinDigitalInput pinEcho = null;
    private GpioPinDigitalOutput pinTrigger = null;
    
    private long distance = 0;
    
    public DistanceSensor() {
        gpio = GpioFactory.getInstance();

        pinEcho = gpio.provisionDigitalInputPin(RaspiPin.GPIO_24, PinPullResistance.PULL_DOWN);
        pinEcho.setShutdownOptions(true);

        pinTrigger = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23);
        pinTrigger.setShutdownOptions(true);    
        
        pinTrigger.low();
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	@Override
	public void run() {
		long pulse_start = 0;
		long pulse_end = 0;
		long pulse_duration = 0;
		
		while(active) {			
			pinTrigger.high();
			try {
				Thread.sleep(0,10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pinTrigger.low();
						
			while(pinEcho.isLow()) {
				pulse_start = System.currentTimeMillis() * 1000;
			}
			
			while(pinEcho.isHigh()) {
				pulse_end = System.currentTimeMillis() * 1000;
			}
			
			pulse_duration = pulse_end - pulse_start;		
			distance = pulse_duration * 17150;
			
			System.out.println("Distance = " + distance);
		}
	}
	
	public long getDistance() {
		return distance;
	}
	
	public void activate() {
		active = true;
	}

	public void deactivate() {
		active = false;
	}
}
