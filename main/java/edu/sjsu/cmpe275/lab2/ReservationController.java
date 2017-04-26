package edu.sjsu.cmpe275.lab2;

import java.util.Comparator;
import java.util.HashSet;
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

	@RequestMapping(
			value = "/{number}", params = "json",
			method = RequestMethod.GET,
			produces = "application/json")
	public @ResponseBody Object getReservationJson(
			@PathVariable String number, HttpServletResponse response)
					throws BadRequestException {
		if(reservationRepository.exists(number)){
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
            HttpServletResponse response){

		Passenger passenger = passengerRepository.findOne(passengerId);
		if(passenger==null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			throw new BadRequestException(passengerId, "Passenger with id");
		}
		Reservation reservation = new Reservation();
		return storeReservation(reservation, passenger, flightLists, response);
		//return reservationRepository.findOne(reservation.getOrderNumber());
	}
	
	//TODO may only exist one added or removed parameter
	@RequestMapping(
			value = "/{number}",
			params= {"flightsAdded", "flightsRemoved"},
			method = RequestMethod.POST,
			produces = "application/json")
	public @ResponseBody Object updateReservation(
			@PathVariable String number,
			@RequestParam("flightsAdded") String[] flightsAdded,
            @RequestParam("flightsRemoved") String[] flightsRemoved,
            HttpServletResponse response){
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
			params= {"passengerId", "from", "to", "flightNumber"},
			method = RequestMethod.GET,
			produces = "application/xml")
	public @ResponseBody Object searchReservation(  
			@RequestParam("passengerId") int passengerId,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("flightNumber") String flightNumber){
		
		return searchReservation(flightNumber, from, to, passengerId); // TODO unfinished
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
			reservation.setPassenger(null);
			Set<Flight> flights = reservation.getFlights();
			for(Flight f : flights) {
				f.getPassengers().remove(reservation.getPassenger());
				f.setSeatsLeft(f.getSeatsLeft()+1);
			}
			
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
			String[] flightLists, HttpServletResponse response){
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
		if(f1.size()!=flightLists.length){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			throw new BadRequestException("Some flight in your reservation is not existing",404);
		}
		//DONE check overlapping
		if(!overlapping(f1,null,null)){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			throw new BadRequestException("Flights are overlapping",400);
		}
		
		//TODO check if the data is duplicated
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
			String[] flightsRemoved, HttpServletResponse response){
		if(flightsAdded.length==0&&flightsRemoved.length==0){
			return reservation;
		}
		Set<Flight> fAdd = new HashSet<Flight>();  
		Set<Flight> fRem = new HashSet<Flight>(); 
		Set<Flight> flights = reservation.getFlights();
		Passenger passenger = reservation.getPassenger();
		int price = reservation.getPrice();
		if(flightsAdded.length!=0){
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
		if(flightsRemoved.length!=0){
			for(String f : flightsRemoved){
				if(flightRepository.exists(f)){
					fRem.add(flightRepository.findOne(f));
				}
			}
		}

		//DONE overlapping check
		if(!overlapping(fAdd, fRem, flights)){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			throw new BadRequestException("Existing overlapped flights",404);
		}

		//DONE judge if the flight,reservation data is duplicated
		for(String flightNum : flightsRemoved){
			Flight flight = flightRepository.findOne(flightNum);
			Set<Passenger> passengers = flight.getPassengers();

			if(!passengers.contains(passenger)){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				throw new BadRequestException("passenger not in",400); // DONE bad request
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

		for(String flightNum : flightsAdded){
			Flight flight = flightRepository.findOne(flightNum);
			Set<Passenger> passengers = flight.getPassengers();

			if(passengers.contains(passenger)){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				throw new BadRequestException("passenger already in",400); // TODO bad request
			} else {
				int seat = flight.getSeatsLeft();
				System.out.println("seat" + seat);
				if(seat == 0) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					throw new BadRequestException("Flight is full",400);//TODO bad request
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
	
	//non-overlapping judgement
	private boolean overlapping(Set<Flight> addedFlights, Set<Flight> removedFlights, Set<Flight> reservatedFlights){
	//addedFlights is the list you want to add into a exited reservation or a new reservation
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
				if(!previousFlight.getArrivalTime().before(flight.getDepartureTime())){
			 		//System.out.println("times are overlap");
			 		return false;
				}
			}
			previousFlight = flight;
		}
		return true;
	}
	
	@SuppressWarnings("unused")
	private Object searchReservation(String flightNumber, String from, String to, Integer passengerId){
		Flight flight = null;
		Passenger passenger = null;
		Set<Reservation> reservations = new HashSet<Reservation>();
		
		if(passengerId != null){
			passenger = passengerRepository.findOne(passengerId);
			if(passenger == null) {
				return "bad request"; //TODO
			}
			passenger.removeCircle();
			reservations = passenger.getReservations();
			if(reservations == null)
				return "result is empty ";
    	}
		if(flightNumber != null){
			flight = flightRepository.findOne(flightNumber);
    		if(flight == null) {
    			return "bad request"; //TODO
    		}
    		flight.removeCircle();
    		if(reservations != null){
    			for(Reservation re : reservations){
    				if(!re.getFlights().contains(flight))
    					reservations.remove(re);
    			}
    			if(reservations == null){
    				return "result is empty ";
    			}
    		} else {
    			for(Reservation re : reservationRepository.findAll()){
    				for(Flight f : re.getFlights()){
    					 if(f.equals(flight))
    						 reservations.add(re); //TODO get reservation
    				}
    			}
    		}
    	}
		
    	if(from != null){
    		if(reservations == null){
    			return "2";// TODO get reservation;
    		}
    	}
    	if(to != null){
    		if(reservations == null){
    			return "3";// TODO get reservation;
    		}
    	}
        return reservations;
	}
}
