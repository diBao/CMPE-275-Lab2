package edu.sjsu.cmpe275.lab2;

//This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
//CRUD refers Create, Read, Update, Delete

import org.springframework.data.repository.CrudRepository;

public interface FlightRepository extends CrudRepository<Flight, String>{

}
