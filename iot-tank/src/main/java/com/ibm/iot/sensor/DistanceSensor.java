package com.ibm.iot.sensor;

import java.util.concurrent.TimeoutException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class DistanceSensor implements Runnable {

	private final static double SOUND_SPEED = 340.29; // speed of sound in m/s

	private final static int TRIG_DURATION_IN_MICROS = 10; // trigger duration
															// of 10 micro s
	private final static int WAIT_DURATION_IN_MILLIS = 60; // wait 60 milli s

	private final static int TIMEOUT = 2100;

	private boolean active = true;

	private GpioController gpio = null;
	private GpioPinDigitalInput pinEcho = null;
	private GpioPinDigitalOutput pinTrigger = null;

	private double distance = 0;

	public DistanceSensor() {
		com.pi4j.wiringpi.Gpio.wiringPiSetup();

		gpio = GpioFactory.getInstance();

		pinTrigger = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_14,
				"Sensor Trigger", PinState.LOW);

		pinEcho = gpio.provisionDigitalInputPin(RaspiPin.GPIO_10,
				"Sensor Result", PinPullResistance.PULL_DOWN);
	}

	@Override
	public void run() {
		try {
			while (active) {
				System.out.println("Range Sensor Triggered");

				long start = 0;

				pinTrigger.high();
				Thread.sleep(10);
				pinTrigger.low();

				while (pinEcho.isLow()) {
					start = System.nanoTime();
				}

				while (pinEcho.isHigh()) {
				}

				distance = (System.nanoTime() - start) / 58000;

				System.out.println("Distance = " + distance);

				try {
					Thread.sleep(WAIT_DURATION_IN_MILLIS);
				} catch (InterruptedException ex) {
					System.err.println("Interrupt during trigger");
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Timeout measuring distance.");
			e.printStackTrace();
		}
	}

	/**
	 * Put a high on the trig pin for TRIG_DURATION_IN_MICROS
	 */
	private void triggerSensor() {
		try {
			this.pinTrigger.high();
			Thread.sleep(0, TRIG_DURATION_IN_MICROS * 1000);
			this.pinTrigger.low();
		} catch (InterruptedException ex) {
			System.err.println("Interrupt during trigger");
		}
	}

	/**
	 * Wait for a high on the echo pin
	 * 
	 * @throws DistanceMonitor.TimeoutException
	 *             if no high appears in time
	 */
	private void waitForSignal() throws TimeoutException {
		int countdown = TIMEOUT;

		while (this.pinEcho.isLow() && countdown > 0) {
			countdown--;
		}

		if (countdown <= 0) {
			throw new TimeoutException("Timeout waiting for signal start");
		}
	}

	/**
	 * @return the duration of the signal in micro seconds
	 * @throws DistanceMonitor.TimeoutException
	 *             if no low appears in time
	 */
	private long measureSignal() throws TimeoutException {
		int countdown = TIMEOUT;
		long start = System.nanoTime();
		while (this.pinEcho.isHigh() && countdown > 0) {
			countdown--;
		}
		long end = System.nanoTime();

		if (countdown <= 0) {
			throw new TimeoutException("Timeout waiting for signal end");
		}

		return (long) Math.ceil((end - start) / 1000.0); // Return micro seconds
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
