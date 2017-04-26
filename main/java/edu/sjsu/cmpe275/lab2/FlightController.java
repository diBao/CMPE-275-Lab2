package edu.sjsu.cmpe275.lab2;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@RestController
@RequestMapping("/flight")
public class FlightController {
	//@Autowired
	//private ReservationRepository reservationRepository;
	//@Autowired
	//private PassengerRepository passengerRepository;
	@Autowired
	private FlightRepository flightRepository;

	String format = "yyyy-MM-dd-HH";

	private Object storeFlight(Flight flight, Integer price, String from_,
			String to_, String departureTime, String arrivalTime, String description,
			Integer capacity, String model, String manufacturer, Integer yearOfManufacture,HttpServletResponse response) throws BadRequestException, ParseException{
		if(from_==null||to_==null||from_==""||to_==""||departureTime==null||arrivalTime==null
				||departureTime==""||arrivalTime==""||description==null||description==""
				||model==null||model==""||manufacturer==null||manufacturer==""
				||price==null||capacity==null||yearOfManufacture==null){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			throw new BadRequestException( "Lack of parameters", 400);
		}
		Plane plane = new Plane(capacity, model, manufacturer, yearOfManufacture);

		SimpleDateFormat sdf = new SimpleDateFormat(format);

		Date dateD = sdf.parse(departureTime);
		Date dateA = sdf.parse(arrivalTime);
		
		//TODO overlapping check error code 400
		
		if(flight.getPlane().getCapacity() != capacity){
			int leftS = flight.getSeatsLeft();
			leftS += (flight.getPlane().getCapacity() - capacity);
			if(leftS >= 0){
				flight.setSeatsLeft(leftS);
			} else {
				//TODO exception: left seat is lower than zero
			}
		}

		flight.setPrice(price);
		flight.setFrom(from_);
		flight.setTo(to_);
		flight.setDepartureTime(dateD);
		flight.setArrivalTime(dateA);
		flight.setDescription(description);
		flight.setPlane(plane);

		Flight f = flightRepository.save(flight);
		
		if(f==null){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			throw new BadRequestException(flight.getNumber(), "Airline with flightNumber");
		}

		f.removeCircle();

		return f;
	}
	
	@RequestMapping(value = "/{flightNumber}",
			params = "json",
			method = RequestMethod.GET,
			produces = "application/json")
	public Object getFlightJSON(@PathVariable String flightNumber,
			@RequestParam Boolean json, HttpServletResponse response) throws BadRequestException{
		if(json.equals(true)){
			Flight flight = flightRepository.findOne(flightNumber);
			if(flight!=null){
				flight.removeCircle();
				return flight;
				}else{
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				throw new BadRequestException(flightNumber, "Airline with flightNumber");//DONE
			}
		}
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		throw new BadRequestException("Parameter JSON error", 400);//DONE
	}
	
	@RequestMapping(value = "/{flightNumber}",
			params = "xml", 
			method = RequestMethod.GET, 
			produces = "application/xml")
	public Object getFlightXML(@PathVariable String flightNumber,
			@RequestParam Boolean xml, HttpServletResponse response) throws BadRequestException{
		if(xml.equals(true)) {
			Flight flight = flightRepository.findOne(flightNumber);
			if(flight!=null){
				flight.removeCircle();
				return flight;
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				throw new BadRequestException(flightNumber, "Airline with flightNumber");//DONE
			}
		}
		
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		throw new BadRequestException("Parameter XML error", 400);//DONE
	}
	
	@RequestMapping(
			value = "/{flightNumber}",method = RequestMethod.POST,
			params= {"price", "from", "to", "departureTime", "arrivalTime", "description",
					"capacity", "model", "manufacturer", "yearOfManufacture"},
			produces = "application/xml")
	public @ResponseBody Object createReservation(
			@PathVariable String flightNumber,
			@RequestParam("price") int price,
            @RequestParam("from") String from_,
            @RequestParam("to") String to_,
            @RequestParam("departureTime") String departureTime,
            @RequestParam("arrivalTime") String arrivalTime,
            @RequestParam("description") String description,
            @RequestParam("capacity") int capacity,
            @RequestParam("model") String model,
            @RequestParam("manufacturer") String manufacturer,
            @RequestParam("yearOfManufacture") int yearOfManufacture,
            HttpServletResponse response) throws ParseException{

		Flight flight;

		if(flightRepository.exists(flightNumber)){
			flight = flightRepository.findOne(flightNumber);
		} else {
			flight = new Flight();
			flight.setNumber(flightNumber);
			flight.setSeatsLeft(capacity);
		}

		return storeFlight(flight, price, from_, to_, departureTime, arrivalTime, description,
				capacity, model, manufacturer, yearOfManufacture, response);

		/* XML output
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String dateD = sdf.format(flight.getDepartureTime());
		String dateA = sdf.format(flight.getArrivalTime());
		*/
//		
//		return f;
	}
	
	@RequestMapping(value = "/{flightNumber}",
			method = RequestMethod.DELETE,
			produces="application/json")
	public  Object  deleteAirline(@PathVariable String flightNumber, HttpServletResponse response)throws BadRequestException {
		//successDelete is get from searching of database
		try{
			if(!flightRepository.findOne(flightNumber).getPassengers().isEmpty()){
				flightRepository.delete(flightNumber);
			} else {
				//TODO You can not delete a flight that has one or more reservation, 
				//in which case, the deletion should fail with error code 400. 
			}
		}catch(Exception e){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			throw new BadRequestException(flightNumber, "Airline with flightNumber");
		}
		response.setStatus(HttpServletResponse.SC_OK);
		return new Response(HttpServletResponse.SC_OK, "Airline with flightNumber "+flightNumber+" is deleted successfully");
	}
}
