package edu.sjsu.cmpe275.lab2;

//import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND)
public class BadRequestException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object ID;//if -1 means no id involved 
	private String errorMsg;
	private int statusCode;
	public BadRequestException(Object id, String str) {
		// TODO Auto-generated constructor stub
		super();
		this.setID(id);
		this.errorMsg = str;
	}
	public BadRequestException( String str, int statusCode) {
		// TODO Auto-generated constructor stub
		super();
		this.errorMsg = str;
		this.statusCode = statusCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public Object getID() {
		return ID;
	}
	public void setID(Object iD) {
		ID = iD;
	}
	
}
