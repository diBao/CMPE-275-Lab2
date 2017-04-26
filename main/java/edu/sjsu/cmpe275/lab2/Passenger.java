package edu.sjsu.cmpe275.lab2;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

//import javax.xml.bind.annotation.XmlTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="Passenger")
@XmlRootElement(name = "Passenger")
@JsonRootName(value = "Passenger")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Passenger {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private int id;
	
	@Column(name = "FIRST_NAME", nullable = false)
	private String firstName;
	
	@Column(name = "LAST_NAME", nullable = false)
	private String lastName;
	
	@Column(name = "AGE")
	private int age;
	
	@Column(name = "GENDER")
	private String gender;
	
	@Column(name = "PHONE", nullable = false, unique = true)
	private String phone;//must be unique
	
	//@OneToMany(mappedBy = "passenger")
	//private Set<Reservation> reservations = new HashSet<Reservation>();
	//@OneToMany(targetEntity = Reservation.class, mappedBy = "passenger")
	//private Set<Reservation> reservations = new HashSet<Reservation>();
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "passenger")
	private Set<Reservation> reservations = new HashSet<Reservation>();
	
	public Passenger(){
	}
	public Passenger(String _firstname, String _lastname, int _age, String _gender, String _phone){
		this.firstName = _firstname;
		this.lastName = _lastname;
		this.age = _age;
		this.gender = _gender;
		this.phone = _phone;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}

	public Set<Reservation> getReservations() {
		return reservations;
	}
	public void setReservations(Set<Reservation> reservations) {
		this.reservations = reservations;
	}
	
	public void removeCircle() {
        
        Iterator<Reservation> reIt = this.getReservations().iterator();
        Iterator<Flight> flIt = null;
        Reservation reservation = null;
        
        while(reIt.hasNext()){ 
            reservation = reIt.next(); 
            reservation.setPassenger(null);
            flIt = reservation.getFlights().iterator();
            while(flIt.hasNext()){ 
            	flIt.next().setPassengers(null); 
            }

        }
    }
	
}
