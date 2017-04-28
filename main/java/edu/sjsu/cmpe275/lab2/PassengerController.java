package edu.sjsu.cmpe275.lab2;

import java.util.Set;

import javax.servlet.http.HttpServletResponse;

//import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@RestController
@RequestMapping(value = "/passenger")
public class PassengerController {
	@Autowired
	private PassengerRepository passengerRepository;
	@Autowired
	private ReservationRepository reservationRepository;
	
	private Object storePassenger(Passenger passenger, String firstname, String lastname, 
			Integer age, String gender, String phone,HttpServletResponse response) throws BadRequestException{
		//DONE BAD Request one
		if(firstname==null||lastname==null||firstname==""||lastname==""
				||age==null||gender==null||gender==""||phone==null||phone==""){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			throw new BadRequestException( "Lack of parameters", 400);
		}
		passenger.setFirstName(firstname);
		passenger.setLastName(lastname);
		passenger.setAge(age);
        passenger.setGender(gender);
		passenger.setPhone(phone);
	
		
		Passenger pa = null;
		try{
			pa = passengerRepository.save(passenger);
		} catch(Exception e){
			 //DONE BAD request to database
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 throw new BadRequestException("Duplicate phone number",400);
		}
		pa.removeCircle();
		return pa;
	}
	
	@RequestMapping(
			value = "/{id}", 
			method = RequestMethod.GET,
			produces = "application/json")
	public @ResponseBody Object getPassengerJson(
			@PathVariable int id, 
			HttpServletResponse response) throws BadRequestException {
		
		Passenger passenger = passengerRepository.findOne(id);
		
		if(passenger!=null){
			passenger.removeCircle();
			return passenger; 
		}else{
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			throw new BadRequestException(id, "Sorry, the requested passenger with id");//DONE
		}
		
		//response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		//throw new BadRequestException("Parameter JSON error", 400);//DONE
	}
	
	@RequestMapping(
			value = "/{id}", 
			params= "xml", 
			method = RequestMethod.GET,
			produces = "application/xml")
	public @ResponseBody Object getPassengerXml(
			@PathVariable int id, 
			@RequestParam Boolean xml,
			HttpServletResponse response) throws BadRequestException{
		if(xml.equals(true)){
			Passenger passenger = passengerRepository.findOne(id);
			
			if(passenger!=null){
				passenger.removeCircle();
				return passenger; //new Passenger(id, "Bob", "Dylan", 56, "M", "733223423"); //String.format(template, name));
			}else{
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				throw new BadRequestException(id, "Sorry, the requested passenger with id");//DONE
			}
		}
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		throw new BadRequestException( "Parameter XML error", 400);//DONE
		//return "bad request"; //String.format(template, name)); 
	}
	
	@RequestMapping(
			value = "",
			params= {"firstname", "lastname", "age", "gender", "phone"},
			method = RequestMethod.POST,
			produces = "application/json")
	public @ResponseBody Object createPassenger(
			@RequestParam("firstname") String firstname, 
            @RequestParam("lastname") String lastname,
            @RequestParam("age") int age,
            @RequestParam("gender") String gender, 
            @RequestParam("phone") String phone,
            HttpServletResponse response){
				
		Passenger passenger = new Passenger();
		return storePassenger(passenger, firstname, lastname, age, gender, phone, response);
		
		//return passengerRepository.findOne(passenger.getId());
	}
	
	@RequestMapping(
			value = "/{id}",
			params= {"firstname", "lastname", "age", "gender", "phone"},
			method = RequestMethod.PUT,
			produces = "application/json")
	public @ResponseBody Object updatePassenger(
			@PathVariable int id,
			@RequestParam("firstname") String firstname, 
            @RequestParam("lastname") String lastname,
            @RequestParam("age") int age,
            @RequestParam("gender") String gender, 
            @RequestParam("phone") String phone,
            HttpServletResponse response) throws BadRequestException{
				
		Passenger passenger = passengerRepository.findOne(id);
		if(passenger==null){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			throw new BadRequestException(id, "Passenger with id");
		}
		storePassenger(passenger, firstname, lastname, age, gender, phone, response);
		
		return passengerRepository.findOne(id);
	}
	
	@RequestMapping(
			value = "/{id}",
			method = RequestMethod.DELETE,
			produces = "application/xml")
	public @ResponseBody ResponseEntity<?> deletePassenger(@PathVariable int id,
            HttpServletResponse response)throws BadRequestException{
		if(!passengerRepository.exists(id)){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			throw new BadRequestException(id, "Passenger with id");
		}
		
		Passenger pa = passengerRepository.findOne(id);
		Set<Reservation> res = pa.getReservations();
		for(Reservation re : res) {
			Set<Flight> fls = re.getFlights();
			for(Flight fl : fls){
				Set<Passenger> pas = fl.getPassengers();
				pas.remove(pa);
				fl.setPassengers(pas);
				fl.setSeatsLeft(fl.getSeatsLeft()+1);
			}
			re.setPassenger(null);
			reservationRepository.delete(re);
		}
		
		passengerRepository.delete(id);
			
		return new ResponseEntity<>(new Response(200, "Passenger with id "+id+" is deleted successfully "),HttpStatus.ACCEPTED);	
	}
}
