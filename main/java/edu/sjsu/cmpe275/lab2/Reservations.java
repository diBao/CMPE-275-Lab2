package edu.sjsu.cmpe275.lab2;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

@XmlRootElement()
//@JsonRootName()
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reservations {
	private Set<Reservation> reservations = new HashSet<Reservation>();

	public Set<Reservation> getReservations() {
		return reservations;
	}

	public void setReservations(Set<Reservation> reservations) {
		this.reservations = reservations;
	}
}
