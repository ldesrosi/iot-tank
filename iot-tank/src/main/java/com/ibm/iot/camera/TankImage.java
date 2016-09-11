package com.ibm.iot.camera;

import java.io.Serializable;

public class TankImage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 239951266980482189L;
	private String type = "image";
	private long timestamp = System.currentTimeMillis();
	private long sessionId = 0;
	private String _doc;
	private String _rev;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String get_doc() {
		return _doc;
	}
	public void set_doc(String _doc) {
		this._doc = _doc;
	}
	public String get_rev() {
		return _rev;
	}
	public void set_rev(String _rev) {
		this._rev = _rev;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public long getSessionId() {
		return sessionId;
	}
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	
}
