package edu.sjsu.cmpe275.lab2;

/*
 * Definition of plane object
 */

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlRootElement;

@Embeddable
@XmlRootElement
public class Plane {
	
	@Column(name = "CAPACITY")
	private int capacity;
	
	@Column(name = "MODEL", nullable = false)
	private String model;
	
	@Column(name = "MANUFACTURER", nullable = false)
	private String manufacturer;
	
	@Column(name = "YEAR_OF_MANUFACTURE")
	private int yearOfManufacture;
	
	public Plane(){
		super();
	}
	
	public Plane(int _capacity, String _model, String _manufacturer, int _yearOfManufacture){
		this.capacity = _capacity;
		this.model = _model;
		this.manufacturer = _manufacturer;
		this.yearOfManufacture = _yearOfManufacture;
	}
	
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public int getYearOfManufacture() {
		return yearOfManufacture;
	}
	public void setYearOfManufacture(int yearOfManufacture) {
		this.yearOfManufacture = yearOfManufacture;
	}
}
