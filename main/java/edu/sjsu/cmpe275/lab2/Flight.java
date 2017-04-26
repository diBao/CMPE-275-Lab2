package edu.sjsu.cmpe275.lab2;

import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

//import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
//import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

@Entity
@Table(name = "Flight")
@XmlRootElement(name = "Flight")
@JsonRootName(value = "Flight")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Flight {
	@Id
	@Column(name = "NUMBER")
	private String number; // Each flight has a unique flight number.

	@Column(name = "PRICE")
	private int price;

	@Column(name = "FROM_", nullable = false)
	private String from;

	@Column(name = "TO_", nullable = false)
	private String to;

	/* Date format: yy-mm-dd-hh, do not include minutes and sceonds.
	** Example: 2017-03-22-19
	*The system only needs to supports PST. You can ignore other time zones.
	*/
	
	//TODO change type to String
	//@JsonFormat(pattern="yyyy-MM-dd-HH")
	@Column(name = "DEPARTURE_TIME", nullable = false)
	private Date departureTime;

	//@JsonFormat(pattern="yyyy-MM-dd-HH")
	@Column(name = "ARRIVAL_TIME", nullable = false)
	private Date arrivalTime;

	@Column(name = "SEATS_LEFT")
	private int seatsLeft;

	@Column(name = "DESCRIPTION")
	private String description;

	@Embedded
	private Plane plane; // Embedded

	@ManyToMany
	@JoinTable(name = "flight_passengers",
				joinColumns = @JoinColumn(name = "flight_id", referencedColumnName="number") ,
				inverseJoinColumns = @JoinColumn(name = "passenger_id", referencedColumnName="id"))
	private Set<Passenger> passengers = new HashSet<Passenger>();

	//@Column(name = "PASSENGER_ID")
	//private int passengerId;

	public Flight(){

	}

	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public Date getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(Date departureTime) {
		this.departureTime = departureTime;
	}
	public Date getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public int getSeatsLeft() {
		return seatsLeft;
	}
	public void setSeatsLeft(int seatsLeft) {
		this.seatsLeft = seatsLeft;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Passenger> getPassengers() {
		return passengers;
	}
	public void setPassengers(Set<Passenger> passengers) {
		this.passengers = passengers;
	}

	public Plane getPlane() {
		return plane;
	}

	public void setPlane(Plane plane) {
		this.plane = plane;
	}

	public void removeCircle() {

        Iterator<Passenger> pIt = this.getPassengers().iterator();
        Passenger passenger = null;

        while(pIt.hasNext()) {
            passenger = pIt.next();
            passenger.setReservations(null);
        }
    }
}
