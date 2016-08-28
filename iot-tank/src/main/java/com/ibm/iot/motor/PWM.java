package com.ibm.iot.motor;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.system.SystemInfo;

public class PWM {
	// Registers/etc.
	private int __MODE1 = 0x00;
	private int __MODE2 = 0x01;
//	private int __SUBADR1 = 0x02;
//	private int __SUBADR2 = 0x03;
//	private int __SUBADR3 = 0x04;
	private int __PRESCALE = 0xFE;
	private int __LED0_ON_L = 0x06;
	private int __LED0_ON_H = 0x07;
	private int __LED0_OFF_L = 0x08;
	private int __LED0_OFF_H = 0x09;
//	private int __ALL_LED_ON_L = 0xFA;
//	private int __ALL_LED_ON_H = 0xFB;
//	private int __ALL_LED_OFF_L = 0xFC;
//	private int __ALL_LED_OFF_H = 0xFD;

	// Bits
//	private byte __RESTART = (byte) 0x80;
	private byte __SLEEP = (byte) 0x10;
	private byte __ALLCALL = (byte) 0x01;
//	private byte __INVRT = (byte) 0x10;
	private byte __OUTDRV = (byte) 0x04;

	// get the I2C bus to communicate on
	private I2CBus i2c = null;

	// create an I2C device for an individual device on the bus that you want to
	// communicate with
	// in this example we will use the default address for the TSL2561 chip
	// which is 0x39.
	private I2CDevice device = null;

	private boolean debug = true;

	public PWM() throws MotorException {
		this(0x60, 1600, false);

	}

	public PWM(int address, float frequency, boolean debug)
			throws MotorException {
		this.debug = debug;
		try {
			i2c = I2CFactory.getInstance(getBusNumber());
			device = i2c.getDevice(address);
			
			System.out.println("Active Device : " + device);
		} catch (UnsupportedBusNumberException | IOException e) {
			throw new MotorException("Exception creating I2C device", e);
		}
		setAllPWM(0, 0);

		try {
			device.write(__MODE2, __OUTDRV);
			device.write(__MODE1, __ALLCALL);
			Thread.sleep(5);

			int mode1 = device.read(__MODE1);
			mode1 = mode1 & ~__SLEEP;
			device.write(__MODE1, (byte) mode1);
			Thread.sleep(5);
		} catch (IOException | InterruptedException e) {
			throw new MotorException("Failed initializing device at address"
					+ address, e);
		}
		setPWMFreq(frequency);
	}

	public void setPWMFreq(float frequency) throws MotorException {
		double prescaleval = 25000000.0;
		prescaleval /= 4096.0;
		prescaleval /= frequency;
		prescaleval -= 1.0;
		double prescale = Math.floor(prescaleval + 0.5);

		if (debug) {
			System.out.println("Setting PWM frequency to " + frequency + " Hz");
			System.out.println("Estimated pre-scale: " + prescaleval);
			System.out.println("Final pre-scale: " + prescale);
		}

		try {
			int oldmode = device.read(__MODE1);
			int newmode = (oldmode & 0x7F) | 0x10;

			device.write(__MODE1, (byte) newmode);
			device.write(__PRESCALE,
					(byte) new Integer((int) Math.floor(prescale)).intValue());
			device.write(__MODE1, (byte) oldmode);
			Thread.sleep(5);
			device.write(__MODE1, (byte) (oldmode | 0x80));

		} catch (IOException | InterruptedException e) {
			throw new MotorException("Exception while setting frequency", e);
		}

	}

	public void setPWM(int channel, int on, int off) throws MotorException {
		try {
			device.write(__LED0_ON_L + 4 * channel, (byte) (on & 0xFF));
			device.write(__LED0_ON_H + 4 * channel, (byte) (on >> 8));
			device.write(__LED0_OFF_L + 4 * channel, (byte) (off & 0xFF));
			device.write(__LED0_OFF_H + 4 * channel, (byte) (off >> 8));
		} catch (IOException e) {
			throw new MotorException("Exception setting channel " + channel, e);
		}
	}

	public void setAllPWM(int on, int off) throws MotorException {
		try {
			device.write(__LED0_ON_L, (byte) (on & 0xFF));
			device.write(__LED0_ON_H, (byte) (on >> 8));
			device.write(__LED0_OFF_L, (byte) (off & 0xFF));
			device.write(__LED0_OFF_H, (byte) (off >> 8));
		} catch (IOException e) {
			throw new MotorException("Exception setting all channel ", e);
		}

	}

	private static int getRevision() throws MotorException{
		try {
			String rev = SystemInfo.getRevision();

			System.out.println("Hardware Revision :  " + rev);

			return (rev.indexOf("0000 0002 0003") != -1) ? 1 : 2;
		} catch (Exception ex) {
			throw new MotorException("Exception retrieving revision", ex);
		} 
	}

	private static int getBusNumber() throws MotorException {
		int busNumber = getRevision() - 1;
		System.out.println("Bus Number : " + busNumber);
		return busNumber;
	}
}
