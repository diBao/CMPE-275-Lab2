package edu.sjsu.cmpe275.lab2;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
//import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@Table(name = "Reservation")
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reservation {
	@Id
	@GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
	@Column(name = "ORDER_NUMBER", length = 32)
    private String orderNumber;
    
	@Column(name = "PRICE")
    private int price; // sum of each flight’s price.
	
	//@ManyToOne(targetEntity = Passenger.class, fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	//@JoinColumn(name = "PASSENGER_ID", referencedColumnName="ID")
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="passenger_id", referencedColumnName="ID")
	private Passenger passenger = null;
    
	@ManyToMany
	@JoinTable(name = "reservations_flights", 
				joinColumns = @JoinColumn(name = "reservation_id", referencedColumnName="ORDER_NUMBER"), 
				inverseJoinColumns = @JoinColumn(name = "flight_id", referencedColumnName="NUMBER"))
	private Set<Flight> flights = new HashSet<Flight>();
    
    //private int flightId;
  
    public Reservation(){
	}
	public Reservation(String _orderNumber, int _price){//, Passenger _passenger){
		this.orderNumber = _orderNumber;
		//this.passenger = _passenger;
		this.price = _price;
	}
    
	public String getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}
	
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	
	public Passenger getPassenger() {
		return passenger;
	}
	public void setPassenger(Passenger passenger) {
		this.passenger = passenger;
	}
	
	public Set<Flight> getFlights() {
		return flights;
	}
	public void setFlights(Set<Flight> flights) {
		this.flights = flights;
	}
	
	public void removeCircle() {
        Iterator<Flight> flIt = this.getFlights().iterator();;
        
        this.getPassenger().setReservations(null); // fetch passenger
        
        while(flIt.hasNext()){ 
        	flIt.next().setPassengers(null); 
        }
    }
}
