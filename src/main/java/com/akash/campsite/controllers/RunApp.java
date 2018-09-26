package com.akash.campsite.controllers;

/**
 * Created by Kash on 9/22/2018.
 *
 * Application starting point.
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.akash.campsite"})
public class RunApp {

    public static void main(String[] args) {
        SpringApplication.run(RunApp.class, args);
    }
}