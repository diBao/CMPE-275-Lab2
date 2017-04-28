package edu.sjsu.cmpe275.lab2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
//import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@RestController
@RequestMapping("/reservation")
public class ReservationController {
	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private PassengerRepository passengerRepository;
	@Autowired
	private FlightRepository flightRepository;
	
	String format = "yyyy-MM-dd-HH";

	@RequestMapping(
			value = "/{number}",
			method = RequestMethod.GET,
			produces = "application/json")
	public @ResponseBody Object getReservationJson(
			@PathVariable String number, HttpServletResponse response)
					throws BadRequestException {
		if(reservationRepository.exists(number)) {
			Reservation reservation = reservationRepository.findOne(number);
			reservation.removeCircle();
			return reservation;
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			throw new BadRequestException(number, "Reservation with number");
		}
	}

	@RequestMapping(
			value = "/{number}",
			method = RequestMethod.GET,
			params = "xml",
			produces = "application/xml")
	public @ResponseBody Object getReservationXml(
			@PathVariable String number, HttpServletResponse response)
					throws BadRequestException{
		if(reservationRepository.exists(number)){
			Reservation reservation = reservationRepository.findOne(number);
			reservation.removeCircle();
			return reservation;
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			throw new BadRequestException(number, "Reservation with number"); //done
		}
	}

	@RequestMapping(
			value = "",
			params= {"passengerId", "flightLists"},
			method = RequestMethod.POST,
			produces = "application/xml")
	public @ResponseBody Object createReservation(
			@RequestParam("passengerId") int passengerId,
            @RequestParam("flightLists") String[] flightLists,
            HttpServletResponse response) throws ParseException{

		Passenger passenger = passengerRepository.findOne(passengerId);
		if(passenger==null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			throw new BadRequestException(passengerId, "Passenger with id");
		}
		Reservation reservation = new Reservation();
		return storeReservation(reservation, passenger, flightLists, response);
		//return reservationRepository.findOne(reservation.getOrderNumber());
	}
	
	//DONE may only exist one added or removed parameter
	@RequestMapping(
			value = "/{number}", 
			method = RequestMethod.POST,
			produces = "application/json")
	public @ResponseBody Object updateReservation(
			@PathVariable String number,
			@RequestParam(value = "flightsAdded", required = false) String[] flightsAdded,
            @RequestParam(value = "flightsRemoved", required = false) String[] flightsRemoved,
            HttpServletResponse response) throws ParseException{
		if(reservationRepository.exists(number)){
			return storeReservation2(reservationRepository.findOne(number), flightsAdded, flightsRemoved, response);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			throw new BadRequestException(number, "Resevation with number");
		}

		//return reservationRepository.findOne(reservation.getOrderNumber());
	}
	
	@RequestMapping(
			value = "",
			method = RequestMethod.GET,
			produces = "application/xml")
	public @ResponseBody Object searchReservation(  
			@RequestParam(value = "passengerId", required = false) Integer passengerId,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "flightNumber", required = false) String flightNumber,
            HttpServletResponse response){
		if(passengerId != null || from != null || to != null || flightNumber != null){
			return findReservation(flightNumber, from, to, passengerId, response); 
		}
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		throw new BadRequestException("Please specify at least one parameter",400);
		//return "bad request"; //TODO "Please specify at least one parameter"
		//return reservationRepository.findOne(reservation.getOrderNumber());
	}

	//passengerId=XX&from=YY&to=ZZ&flightNumber=123

	@RequestMapping(
			value = "/{number}",
			method = RequestMethod.DELETE,
			produces = "application/xml")
	public @ResponseBody Object deleteReservation(
			@PathVariable String number,
			HttpServletResponse response){
		if(reservationRepository.exists(number)){
			//this is a delete method
			Reservation reservation = reservationRepository.findOne(number);
			Set<Reservation> res = reservation.getPassenger().getReservations();
			res.remove(reservation);
			
			Set<Flight> flights = reservation.getFlights();
			for(Flight f : flights) {
				Set<Passenger> pas = f.getPassengers();
				pas.remove(reservation.getPassenger());
				f.setPassengers(pas);
				f.setSeatsLeft(f.getSeatsLeft()+1);
			}
			reservation.setPassenger(null);
			reservationRepository.delete(number);
			response.setStatus(HttpServletResponse.SC_ACCEPTED);
			return new Response(200, "Delete reservation success"); 
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			throw new BadRequestException( number,"Reservation with number");
		}

		//return reservationRepository.findOne(reservation.getOrderNumber());
	}

	//for createReservation
	private Object storeReservation(Reservation reservation, Passenger passenger,
			String[] flightLists, HttpServletResponse response) throws ParseException{
		if(flightLists.length==0){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			throw new BadRequestException("Passenger reserve no flights",400);
		}
		int price = 0;
		Set<Flight> f1 = new HashSet<Flight>();
		Set<Flight> flights = new HashSet<Flight>();

		for(String f : flightLists){
			if(flightRepository.exists(f)){
				f1.add(flightRepository.findOne(f));
			}
		}
		if(f1 == null || f1.size()!=flightLists.length){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			throw new BadRequestException("Some flight in your reservation is not existing",404);
		}
		//DONE check overlapping
		if(!overlapping(f1,null,null)){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			throw new BadRequestException("Flights are overlapping",400);
		}
		
		//DONE check if the data is duplicated
		for(String flightNum : flightLists){
			Flight flight = flightRepository.findOne(flightNum);
			Set<Passenger> passengers = flight.getPassengers();

			//bad request
			if(passengers.contains(passenger)){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				throw new BadRequestException("You already in one flight of the reservation", 400); //bad request
			} else {
				int seat = flight.getSeatsLeft();
				System.out.println("seat" + seat);
				if(seat == 0) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					throw new BadRequestException("Some flight in your reservation is full, cannot reservate for you",400); //DONE bad request
				} else {
					seat--;
				}
				flight.setSeatsLeft(seat);
				passengers.add(passenger);
			}
			flight.setPassengers(passengers);

			price += flight.getPrice();
			flights.add(flight);
		}

		reservation.setPrice(price);
		reservation.setFlights(flights);
		reservation.setPassenger(passenger);

		Reservation reser = reservationRepository.save(reservation);
		reser.removeCircle();
		return reser;
	}
	
	//for updateReservstion
	private Object storeReservation2(Reservation reservation, String[] flightsAdded,
			String[] flightsRemoved, HttpServletResponse response) throws ParseException{
		if(flightsAdded == null&&flightsRemoved == null){
			return reservation;
		}
		Set<Flight> fAdd = new HashSet<Flight>();  
		Set<Flight> fRem = new HashSet<Flight>(); 
		Set<Flight> flights = reservation.getFlights();
		Passenger passenger = reservation.getPassenger();
		int price = reservation.getPrice();
		if(flightsAdded != null){
			for(String f : flightsAdded){
				if(flightRepository.exists(f)){
					fAdd.add(flightRepository.findOne(f));
				}
			}
			if(fAdd.size()!=flightsAdded.length){
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				throw new BadRequestException("some new flights you want to add are not existing",404);
			}
		}
		if(flightsRemoved != null){
			for(String f : flightsRemoved){
				if(flightRepository.exists(f)){
					fRem.add(flightRepository.findOne(f));
				}
			}
		}

		//DONE overlapping check
		if(fAdd != null && !overlapping(fAdd, fRem, flights)){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			throw new BadRequestException("Existing overlapped flights",404);
		}

		//DONE judge if the flight,reservation data is duplicated
		if(flightsRemoved != null){
			for(String flightNum : flightsRemoved){
				Flight flight = flightRepository.findOne(flightNum);
				Set<Passenger> passengers = flight.getPassengers();
	
				if(!passengers.contains(passenger)){
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					throw new BadRequestException("passenger not in flight " + flightNum,400); // DONE bad request
				} else {
					int seat = flight.getSeatsLeft();
					System.out.println("seat" + seat);
					seat++;
					flight.setSeatsLeft(seat);
					passengers.remove(passenger);
				}
				flight.setPassengers(passengers);
	
				price -= flight.getPrice();
				flights.remove(flight);
			}
		}
		
		if(flightsAdded != null) {
			for(String flightNum : flightsAdded){
				Flight flight = flightRepository.findOne(flightNum);
				Set<Passenger> passengers = flight.getPassengers();
	
				if(passengers.contains(passenger)){
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					throw new BadRequestException("passenger already in flight " + flightNum,400); // DONE bad request
				} else {
					int seat = flight.getSeatsLeft();
					System.out.println("seat" + seat);
					if(seat == 0) {
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						throw new BadRequestException("Flight " + flightNum + " is full",400);//DONE bad request
					} else {
						seat--;
					}
					flight.setSeatsLeft(seat);
					passengers.add(passenger);
				}
				flight.setPassengers(passengers);
	
				price += flight.getPrice();
				flights.add(flight);
			}
		}
		
		reservation.setPrice(price);
		reservation.setFlights(flights);
		reservation.setPassenger(passenger);
		
		Reservation reser = reservationRepository.save(reservation);
		reser.removeCircle();
		return reser;
	}
	
	//non-overlapping judgement
	private boolean overlapping(Set<Flight> addedFlights, Set<Flight> removedFlights, Set<Flight> reservatedFlights) throws ParseException{
	//addedFlights is the list you want to add into a exited reservation or a new reservation
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		PriorityQueue<Flight> queue = new PriorityQueue<>(new Comparator<Flight>(){
	        public int compare(Flight a, Flight b){
	            return a.getDepartureTime().compareTo(b.getDepartureTime());
	        }
	    });
		//addedFlights must be not null
		for(Flight flight: addedFlights){
			queue.add(flight);
		}
		//reservatedFlights may be null
		if(reservatedFlights!=null){
			for(Flight flight:reservatedFlights){
			//removedFlights may be null
				if(removedFlights==null || !removedFlights.contains(flight)){
				 	queue.add(flight);
				}
			}
		}
		Flight previousFlight = null;
		for(Flight flight: queue){
			if(previousFlight!=null){
				if(!(sdf.parse(previousFlight.getArrivalTime())).before(sdf.parse(flight.getDepartureTime()))){
			 		//System.out.println("times are overlap");
			 		return false;
				}
			}
			previousFlight = flight;
		}
		return true;
	}
	
	
	//@SuppressWarnings("unused")
	private Object findReservation(String flightNumber, String from, String to, Integer passengerId, HttpServletResponse response){
		Flight flight = null;
		Passenger passenger = null;
		Reservations reservations = new Reservations();
		Set<Reservation> res_temp = new HashSet<Reservation>();
		
		if(passengerId != null){
			passenger = passengerRepository.findOne(passengerId);
			if(passenger == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				throw new BadRequestException(passengerId, "Passenger with Id "); //DONE
			}
			res_temp = passenger.getReservations();
			if(res_temp == null)//DONE this is response? yes， it is.
				return "No result!";
			reservations.setReservations(res_temp);
			res_temp = new HashSet<Reservation>();
    	}
		//System.out.println(res_temp.size());
		
		
		if(flightNumber != null){
			flight = flightRepository.findOne(flightNumber);
    		if(flight == null) {
    			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				throw new BadRequestException(flightNumber, "Airline with flightNumber "); //DONE
    		}
    		if(!reservations.getReservations().isEmpty()){
    			for(Reservation re : reservations.getReservations()){
    				if(re.getFlights().contains(flight)){
    					res_temp.add(re);
    				}
    			}
    			if(res_temp.isEmpty())
    				return "No result!";
    		} else {
    			//Set<Reservation> res = null;
    			for(Reservation re : reservationRepository.findAll()){
    				for(Flight f : re.getFlights()){
    					if(f.equals(flight)){
    						res_temp.add(re);
    					}
    				}
    			}
    			if(res_temp.isEmpty())
    				return "No result!";
    		}
    		reservations.setReservations(res_temp);
    		res_temp = new HashSet<Reservation>();
    	}
		//System.out.println(res_temp.size());
		
		
		if(from != null){
			if(!reservations.getReservations().isEmpty()){
				for(Reservation re : reservations.getReservations()){
					for(Flight f : re.getFlights()){
						if(f.getFrom().equals(from)){
							res_temp.add(re);
						}
					}
				}
				if(res_temp.isEmpty())
    				return "No result!";
			} else {
				for(Reservation re : reservationRepository.findAll()){
    				for(Flight f : re.getFlights()){
    					if(f.getFrom().equals(from)){
							res_temp.add(re);
						}
    				}
    			}
				if(res_temp.isEmpty())
    				return "No result!";
			}
			
			reservations.setReservations(res_temp);
			res_temp = new HashSet<Reservation>();
    	}
		//System.out.println(res_temp.size());
		
		if(to != null){
			if(!reservations.getReservations().isEmpty()){
				for(Reservation re : reservations.getReservations()){
					for(Flight f : re.getFlights()){
						if(f.getTo().equals(to)){
							res_temp.add(re);
						}
					}
				}
				if(res_temp.isEmpty())
    				return "No result!";
			} else {
				for(Reservation re : reservationRepository.findAll()){
    				for(Flight f : re.getFlights()){
    					if(f.getTo().equals(to)){
							res_temp.add(re);
						}
    				}
    			}
				if(res_temp.isEmpty())
    				return "No result!";
			}
    	}
		//System.out.println(res_temp.size());
		if(res_temp.isEmpty())
			res_temp = reservations.getReservations();
		
		reservations = new Reservations();
		Set<Reservation> res = new HashSet<Reservation>();
		for(Reservation re : res_temp){
			re.removeCircle();
			res.add(re);
		}
		reservations.setReservations(res);
		
		if(reservations.getReservations().isEmpty())
			return "No result!";
		else
			return reservations;
	}
}
