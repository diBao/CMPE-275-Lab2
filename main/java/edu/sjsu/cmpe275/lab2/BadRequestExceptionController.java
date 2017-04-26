package edu.sjsu.cmpe275.lab2;



import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.sun.javafx.logging.Logger;


@ControllerAdvice
public class BadRequestExceptionController {
	
	//private static final Log logging = LogFactory.getLog(BadRequestExceptionController.class);
	@ExceptionHandler(BadRequestException.class)
	@ResponseBody
	@RequestMapping(produces = "application/json")
	public Object handleDeleteError(BadRequestException e){
		BadRequest badRequestReturn = null;
		if(e.getID()!=null){
			badRequestReturn = new BadRequest(HttpServletResponse.SC_NOT_FOUND,e.getErrorMsg()
					+" "+e.getID()+" does not exist ");
		}else{
			badRequestReturn = new BadRequest(e.getStatusCode(),e.getErrorMsg());
		}
		
		return 	badRequestReturn;
	}
}
