package edu.sjsu.cmpe275.lab2;

//import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "BadRequest")
public class BadRequest{
	private int code;
	private String msg;
	public BadRequest(int statusCode, String str) {
		// TODO Auto-generated constructor stub
		this.code = statusCode;
		this.msg = str;
	}
	public void setCode(int code){
		this.code = code;
	}
	
	public int getCode() {
		// TODO Auto-generated method stub
		return this.code;
	}
	public void setMsg(String msg){
		this.msg = msg;
	}
	public String getMsg() {
		// TODO Auto-generated method stub
		return this.msg;
	}
}
