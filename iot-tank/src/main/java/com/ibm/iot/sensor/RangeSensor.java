package com.ibm.iot.sensor;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioUtil;

public class RangeSensor implements Runnable {
	private final static double SOUND_SPEED = 340.29;      // speed of sound in m/s
	private final static int TRIG_DURATION_IN_MICROS = 10; // trigger duration of 10 micro s
	private final static int WAIT_DURATION_IN_MILLIS = 60; // wait 60 milli s

	private final static int TRIGGER_PIN = 23;
	private final static int ECHO_PIN = 24;

	private boolean active = true;
	private double distance = 0;

	private long startTime = 0;
	private long endTime = 0;

	static {
		if (Gpio.wiringPiSetup() == -1) {
            System.out.println(" ==>> GPIO SETUP FAILED");
		}
	}
	
	public RangeSensor() {
		GpioUtil.export(TRIGGER_PIN, GpioUtil.DIRECTION_OUT);
		Gpio.pinMode(TRIGGER_PIN, Gpio.OUTPUT);

		GpioUtil.export(ECHO_PIN, GpioUtil.DIRECTION_IN);
		GpioUtil.setEdgeDetection(ECHO_PIN, GpioUtil.EDGE_BOTH);
		Gpio.pinMode(ECHO_PIN, Gpio.INPUT);
		Gpio.pullUpDnControl(ECHO_PIN, Gpio.PUD_DOWN);
	}

	@Override
	public void run() {
		while (active) {
			System.out.println("Range Sensor Triggered");

			triggerSensor();
			waitForSignal();
			long duration = measureSignal();

			distance = duration * SOUND_SPEED / (2 * 10000);
			
			System.out.println("Distance is: " + distance);

			try {
				Thread.sleep(WAIT_DURATION_IN_MILLIS);
			} catch (InterruptedException ex) {
				System.err.println("Interrupt during trigger");
			}
		}
	}

	/**
	 * Put a high on the trig pin for TRIG_DURATION_IN_MICROS
	 */
	private void triggerSensor() {
		Gpio.digitalWrite(TRIGGER_PIN, 1);
		Gpio.delayMicroseconds(TRIG_DURATION_IN_MICROS);
		Gpio.digitalWrite(TRIGGER_PIN, 0);

	}

	private void waitForSignal() {
		while (Gpio.digitalRead(ECHO_PIN) == 0) {
			startTime = System.nanoTime();
		}
	}

	private long measureSignal() {
		while (Gpio.digitalRead(ECHO_PIN) == 1) {
			endTime = System.nanoTime();
		}

		return (long) Math.ceil((endTime - startTime) / 1000.0); // Return micro
																	// seconds
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
