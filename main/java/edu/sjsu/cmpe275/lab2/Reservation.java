package edu.sjsu.cmpe275.lab2;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
//import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

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
	
	@ManyToOne(targetEntity = Passenger.class, fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	//@JoinColumn(name = "PASSENGER_ID", referencedColumnName="ID")
    private Passenger passenger;
    
    @ManyToMany(targetEntity = Flight.class, fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	/*@JoinTable(
			name = "Flight",
			joinColumns=@JoinColumn(name="NUMBER", referencedColumnName="FLIGHT_ID"))
			*/
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
	/*
	public int getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}
	
	public int getFlightId() {
		return flightId;
	}
	public void setFlightId(int flightId) {
		this.flightId = flightId;
	}
	*/
}
