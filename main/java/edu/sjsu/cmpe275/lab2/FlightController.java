package edu.sjsu.cmpe275.lab2;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;

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
	
	private Object storeFlight(Flight flight, int price, String from_, 
			String to_, String departureTime, String arrivalTime, String description, 
			int capacity, String model, String manufacturer, int yearOfManufacture) throws ParseException{
		
		Plane plane = new Plane(capacity, model, manufacturer, yearOfManufacture);
		
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		
		Date dateD = sdf.parse(departureTime);
		Date dateA = sdf.parse(arrivalTime);
				
		flight.setPrice(price);
		flight.setFrom(from_);
		flight.setTo(to_);
		flight.setDepartureTime(dateD);
		flight.setArrivalTime(dateA);
		flight.setDescription(description);
		flight.setPlane(plane);
		
		flightRepository.save(flight);
		return flight;
	}
	
	@RequestMapping(
			value = "/{flightNumber}",
			params= {"price", "from", "to", "departureTime", "arrivalTime", "description",
					"capacity", "model", "manufacturer", "yearOfManufacture"},
			method = RequestMethod.POST,
			produces = "application/xml")
	public @ResponseBody Flight createReservation(
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
            @RequestParam("yearOfManufacture") int yearOfManufacture) throws ParseException{
				
		Flight flight;
		
		if(flightRepository.exists(flightNumber)){
			flight = flightRepository.findOne(flightNumber);
		} else {
			flight = new Flight();
			flight.setNumber(flightNumber);
			flight.setSeatsLeft(capacity);
		}
		
		storeFlight(flight, price, from_, to_, departureTime, arrivalTime, description,
				capacity, model, manufacturer, yearOfManufacture);
		
		/* XML output
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String dateD = sdf.format(flight.getDepartureTime());
		String dateA = sdf.format(flight.getArrivalTime());
		*/
		
		return flightRepository.findOne(flight.getNumber());
	}
}
