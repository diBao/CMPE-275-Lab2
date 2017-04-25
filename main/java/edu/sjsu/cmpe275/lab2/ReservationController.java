package edu.sjsu.cmpe275.lab2;

import java.util.HashSet;
//import java.util.List;
import java.util.Set;

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
			value = "/{number}", 
			method = RequestMethod.GET,
			produces = "application/json")
	public @ResponseBody Object getReservationJson(
			@PathVariable String number) {
		if(reservationRepository.exists(number)){
			Reservation reservation = reservationRepository.findOne(number);
			
			return removePassenger(reservation);
		} else {
			return "bad request"; //TODO
		}
	}
	
	@RequestMapping(
			value = "/{number}", 
			method = RequestMethod.GET,
			produces = "application/xml")
	public @ResponseBody Object getReservationXml(
			@PathVariable String number) {
		if(reservationRepository.exists(number)){
			Reservation reservation = reservationRepository.findOne(number);
			
			return removePassenger(reservation);
		} else {
			return "bad request"; //TODO
		}
	}
	
	@RequestMapping(
			value = "",
			params= {"passengerId", "flightLists"},
			method = RequestMethod.POST,
			produces = "application/xml")
	public @ResponseBody Object createReservation(
			@RequestParam("passengerId") int passengerId, 
            @RequestParam("flightLists") String[] flightLists){
		
		Passenger passenger = passengerRepository.findOne(passengerId);
		Reservation reservation = new Reservation();
		return storeReservation(reservation, passenger, flightLists);
		//return reservationRepository.findOne(reservation.getOrderNumber());
	}
	
	@RequestMapping(
			value = "/{number}",
			params= {"flightsAdded", "flightsRemoved"}, 
			method = RequestMethod.POST,
			produces = "application/json")
	public @ResponseBody Object updateReservation(
			@PathVariable String number,
			@RequestParam("flightsAdded") String[] flightsAdded, 
            @RequestParam("flightsRemoved") String[] flightsRemoved){
		if(reservationRepository.exists(number)){
			return storeReservation2(reservationRepository.findOne(number), flightsAdded, flightsRemoved);
		} else {
			return "bad request"; //TODO
		}
		
		//return reservationRepository.findOne(reservation.getOrderNumber());
	}
	
	@RequestMapping(
			value = "",
			params= {"passengerId", "from", "to", "flightNumbe"},
			method = RequestMethod.GET,
			produces = "application/xml")
	public @ResponseBody Object searchReservation(  // TODO unfinished
			@RequestParam("passengerId") int passengerId, 
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("flightNumber") String flightNumber){
		
		Passenger passenger = passengerRepository.findOne(passengerId);
		Flight flight = flightRepository.findOne(flightNumber);
		/*
		for(Reservation reservation : passenger.getReservations()){
        	if(reservation.getFlights().contains(flight)){
        		return "Retrieve successfully!!!    Reservation Infomation : " + reservation.toString();
        	}
        }
        */
		return new Reservation(); // fake reponse
		//return reservationRepository.findOne(reservation.getOrderNumber());
	}
	
	//passengerId=XX&from=YY&to=ZZ&flightNumber=123
	
	@RequestMapping(
			value = "/{number}",
			method = RequestMethod.DELETE,
			produces = "application/XML")
	public @ResponseBody Object deleteReservation(
			@PathVariable String number){
		if(reservationRepository.exists(number)){
			return "delete"; //TODO unfinished
		} else {
			return "bad request"; //TODO
		}
		
		//return reservationRepository.findOne(reservation.getOrderNumber());
	}
	
	
	private Object storeReservation(Reservation reservation, Passenger passenger, String[] flightLists){
		int price = 0;
		Set<Flight> f1 = new HashSet<Flight>();  // TODO for check overlapping
		Set<Flight> flights = new HashSet<Flight>();
		
		for(String f : flightLists){
			if(flightRepository.exists(f)){
				f1.add(flightRepository.findOne(f));
			}
		}
		
		//TODO judge if the data is duplicated
		for(String flightNum : flightLists){
			Flight flight = flightRepository.findOne(flightNum);
			Set<Passenger> passengers = flight.getPassengers();
			
			//bad request
			if(passengers.contains(passenger)){
				return "already in"; //bad request
			} else {
				int seat = flight.getSeatsLeft();
				System.out.println("seat" + seat);
				if(seat == 0) {
					return "full"; //TODO bad request
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

		return removePassenger(reser);
	}
	
	private Object storeReservation2(Reservation reservation, String[] flightsAdded, String[] flightsRemoved){
		Set<Flight> fAdd = new HashSet<Flight>();  // TODO for check overlapping
		Set<Flight> fRem = new HashSet<Flight>();  // TODO for check overlapping
		Set<Flight> flights = reservation.getFlights();
		Passenger passenger = reservation.getPassenger();
		int price = reservation.getPrice();
		
		for(String f : flightsAdded){
			if(flightRepository.exists(f)){
				fAdd.add(flightRepository.findOne(f));
			}
		}
		
		for(String f : flightsRemoved){
			if(flightRepository.exists(f)){
				fRem.add(flightRepository.findOne(f));
			}
		}
		
		//TODO overlapping check
		
		
		
		
		//TODO judge if the flight,reservation data is duplicated
		for(String flightNum : flightsRemoved){
			Flight flight = flightRepository.findOne(flightNum);
			Set<Passenger> passengers = flight.getPassengers();
			
			if(!passengers.contains(passenger)){
				return "not in"; // TODO bad request
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
				return "already in"; // TODO bad request
			} else {
				int seat = flight.getSeatsLeft();
				System.out.println("seat" + seat);
				if(seat == 0) {
					return "full"; //TODO bad request
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

		return removePassenger(reservationRepository.save(reservation));
		
	}
	
	private Reservation removePassenger(Reservation reservation){
		Set<Flight> flights = reservation.getFlights();
		for(Flight f : flights) {
			f.setPassengers(null);
		}
		reservation.setFlights(flights);
		
		return reservation;
	}
}
