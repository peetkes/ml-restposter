package com.marklogic.rest;

import org.apache.http.entity.ContentType;

public class MLPayLoad {
	private String uri;
	private String msg;
	private String type;
	
	private enum Type {
		json, xml, text, binary
	}
	
	public MLPayLoad(String uri, String messageText, String type) {
		this.uri = uri;
		this.msg = messageText;
		this.type = type;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getMsg() {
		return msg;
	}
	public boolean hasMsg() {
		if (msg == null) return false;
		else return true;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.msg = type;
	}
	
	public ContentType getContentType() {
		Type tp = Type.valueOf(type);
		switch (tp) {
		case json:
			return ContentType.create("text/json", "UTF-8");
		case xml:
			return ContentType.create("application/xml", "UTF-8");
		default:
			return ContentType.create("text/plain", "UTF-8");
		}
	}
	
}
