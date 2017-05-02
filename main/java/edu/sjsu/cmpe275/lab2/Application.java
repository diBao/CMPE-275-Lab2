package edu.sjsu.cmpe275.lab2;

/*
 * function: start of spring boot application
 */

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@EnableAutoConfiguration
public class Application {
	public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
