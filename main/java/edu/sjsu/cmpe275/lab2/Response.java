package edu.sjsu.cmpe275.lab2;



/* this is uniform success status code */

public class Response {
	private int code;
	private String msg;
	public int getCode() {
		return code;
	}
	public void setCode(int code){
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String str){
		this.msg = str;
	}
	public Response(int statusCode, String str) {
		// TODO Auto-generated constructor stub
		this.code = statusCode;
		this.msg = str;
	}
}
