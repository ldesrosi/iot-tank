package com.ibm.iot.camera;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.enums.Encoding;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;

public class TankVision implements Runnable {
	private static int SLEEP_DURATION = 2000;
	private static int IMAGE_WIDTH = 100;
	private static int IMAGE_HEIGTH = 100;
	
	private RPiCamera piCamera = null;
	private CloudantClient dbClient = null;
	private Database db = null;
	
	private long sessionId = 0;
	private boolean active = true;
	private int imageCounter = 0;
	
	private Thread executionThread = null;

	public void init() throws VisionException {
		try {
			Properties prop = new Properties();
		    try {
				prop.load(TankVision.class.getResourceAsStream("/cloudant.properties"));
			} catch (IOException e) {
				throw new VisionException("Error loading configuration file.", e);
			}
		    
			piCamera = new RPiCamera();
			piCamera.setWidth(IMAGE_WIDTH);
			piCamera.setHeight(IMAGE_HEIGTH);
			piCamera.setEncoding(Encoding.JPG); // Change encoding of images to PNG

			dbClient = ClientBuilder.account(prop.getProperty("account"))
					.username(prop.getProperty("username"))
					.password(prop.getProperty("password"))
					.build();

			db = dbClient.database(prop.getProperty("database"), false);
			
		} catch (FailedToRunRaspistillException e) {
			throw new VisionException("Exception initializing the Pi Camera.", e);
		}
	}
	
	public void activate() {
		executionThread = new Thread(this);
		executionThread.start();
	}
	
	public void deactivate() {
		active = false;
	}
	
	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public void run() {
		System.out.println("Tank Vision Activated:" + active);
		
		BufferedImage buffer = null;
		ByteArrayOutputStream baos = null;
		InputStream is = null;
		Response resp = null;
		
		
		try {
			
			while (active) {
				
				String attachementName = Long.toString(sessionId) + "-" + (++imageCounter);
				
				System.out.println("Taking picture");
				buffer = piCamera.takeBufferedStill();
				System.out.println("Done taking picture");
				
				baos = new ByteArrayOutputStream();
				ImageIO.write(buffer, "jpg", baos);
				is = new ByteArrayInputStream(baos.toByteArray());
				
				System.out.println("Saving Attachment; Size of " + baos.size());
				resp = db.saveAttachment(is, attachementName, "image/jpeg");
				
				System.out.println("Done saving picture");
				if (resp.getError() != null) {
					throw new VisionException("Error occured saving attachment; Error is" + resp.getError() + ". Reason is: " + resp.getReason());
				}
				
				Thread.sleep(SLEEP_DURATION);
			}
		} catch (InterruptedException | IOException | VisionException e) {
			e.printStackTrace();
		}
	}

}
