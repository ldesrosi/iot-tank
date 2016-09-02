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
	private RPiCamera piCamera = null;
	private CloudantClient dbClient = null;
	private Database db = null;
	
	private long sessionId = 0;

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public void init() throws VisionException {
		try {
			Properties prop = new Properties();
		    try {
				prop.load(TankVision.class.getResourceAsStream("/cloudant.properties"));
			} catch (IOException e) {
				throw new VisionException("Error loading configuration file.", e);
			}
		    
			piCamera = new RPiCamera();
			piCamera.setWidth(100);
			piCamera.setHeight(100);
			piCamera.setEncoding(Encoding.JPG); // Change encoding of images to PNG

			dbClient = ClientBuilder.account(prop.getProperty("account"))
					.username(prop.getProperty("username")).password(prop.getProperty("password")).build();

			db = dbClient.database(prop.getProperty("database"), false);
			
		} catch (FailedToRunRaspistillException e) {
			throw new VisionException("Exception initializing the Pi Camera.", e);
		}
	}

	@Override
	public void run() {
		BufferedImage buffer = null;
		ByteArrayOutputStream baos = null;
		InputStream is = null;
		Response resp = null;
		
		String attachementName = "" + sessionId;
		try {
			while (true) {
				System.out.println("Taking picture");
				buffer = piCamera.takeBufferedStill();
				
				System.out.println("Converting to input stream");
				baos = new ByteArrayOutputStream();
				ImageIO.write(buffer, "jpg", baos);
				is = new ByteArrayInputStream(baos.toByteArray());
				
				System.out.println("Saving Attachment; Size of " + baos.size());
				resp = db.saveAttachment(is, attachementName, "image/jpeg");
				
				if (resp.getError() != null) {
					throw new VisionException("Error occured saving attachment; Error is" + resp.getError() + ". Reason is: " + resp.getReason());
				}
				
				Thread.sleep(2000);
			}
		} catch (InterruptedException | IOException | VisionException e) {
			e.printStackTrace();
		}
	}

}
