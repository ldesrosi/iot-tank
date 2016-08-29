package com.ibm.iot.sensor;

import java.util.LinkedList;
import java.util.List;

import com.pi4j.wiringpi.Gpio;

public class RangeSensor implements Runnable {
	private final static double SOUND_SPEED = 340.29;      // speed of sound in m/s
	private final static int TRIG_DURATION_IN_MICROS = 10; // trigger duration of 10 micro s
	private final static int WAIT_DURATION_IN_MILLIS = 200; // 

	private final static int TRIGGER_PIN = 23;
	private final static int ECHO_PIN = 24;

	private boolean active = true;
	private double distance = -1;

	private long startTime = 0;
	private long endTime = 0;
	
	private List<RangeListener> listeners = new LinkedList<RangeListener>();

	static {
		//Using BCM Pin mapping
		if (Gpio.wiringPiSetupGpio() == -1) {
            System.out.println(" ==>> GPIO SETUP FAILED");
		}
	}
	
	public RangeSensor() {
		Gpio.pinMode(TRIGGER_PIN, Gpio.OUTPUT);
		Gpio.pinMode(ECHO_PIN, Gpio.INPUT);
		
		Gpio.digitalWrite(TRIGGER_PIN, 0);
		Gpio.delay(2000);
	}

	@Override
	public void run() {
		System.out.println("Range Sensor Activated");
		while (active) {
			double lastDistance = distance;
			long lastDistanceTime = endTime;
			
			Gpio.digitalWrite(TRIGGER_PIN, 1);
			Gpio.delayMicroseconds(TRIG_DURATION_IN_MICROS);
			Gpio.digitalWrite(TRIGGER_PIN, 0);
			
			while (Gpio.digitalRead(ECHO_PIN) == 0) {
				startTime = System.nanoTime();
			}
			
			while (Gpio.digitalRead(ECHO_PIN) == 1) {
				endTime = System.nanoTime();
			}

			long duration = (long) Math.ceil((endTime - startTime) / 1000.0); 

			distance = duration * SOUND_SPEED / (2 * 10000);
			
			dispatchEvents(lastDistance, lastDistanceTime, distance, endTime);
			
			try {
				Thread.sleep(WAIT_DURATION_IN_MILLIS);
			} catch (InterruptedException ex) {
				System.err.println("Interrupt during trigger");
			}
		}
	}

	public void addListener(RangeListener listener) {
		assert(listener != null);
		
		listeners.add(listener);
	}
	
	private void dispatchEvents(double lastDist, long lastDistTime, double dist, long distTime) {
		RangeEvent event = new RangeEvent();
		event.setDistance(dist);
		event.setEventTime(distTime);
		event.setLastDistance(lastDist);
		event.setLastEventTime(lastDistTime);
		
		listeners.forEach(listener->{
			listener.onDistanceChange(event);
		});
	}

	public double getDistance() {
		return distance;
	}

	public void activate() {
		active = true;
	}

	public void deactivate() {
		active = false;
	}

}
