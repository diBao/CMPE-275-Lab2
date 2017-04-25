package edu.sjsu.cmpe275.lab2;

import edu.sjsu.cmpe275.lab2.Passenger;

import org.springframework.data.repository.CrudRepository;

//This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
//CRUD refers Create, Read, Update, Delete

public interface PassengerRepository extends CrudRepository<Passenger, Integer>{

}
