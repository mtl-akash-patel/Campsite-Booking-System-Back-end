package com.akash.campsite.controllers;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.akash.campsite.pojo.Booking;
import com.akash.campsite.pojo.User;
import com.akash.campsite.service.CampsiteService;

import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Created by Kash on 9/22/2018.
 */

@RestController
@RequestMapping(value = "/campsite")
public class CampsiteRestController {

    @Autowired
    private CampsiteService campsiteService;

    /**
     * Rest endpoint for cancelling a booking.
     * @param booking   Booking to be cancelled
     * @return
     */
    @DeleteMapping (value = "/cancel_booking")
    public ResponseEntity<String> cancelBooking(@RequestBody Booking booking) {
        ResponseEntity<String> responseEntity;
        try {
            campsiteService.attemptToCancelBooking(booking.getBookingId());
            responseEntity = ResponseEntity.status(HttpStatus.OK).body("Booking Reference was successfully cancelled");
        }
        catch (IllegalArgumentException | HibernateException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return responseEntity;
    }

    /**
     * Rest endpoint for creating a new user
     * @param user
     * @return
     */
    @PostMapping (value = "/create_user")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        ResponseEntity<String> responseEntity;
        try {
            campsiteService.attemptToCreateUser(user.getFirstName(), user.getLastName(), user.getEmail());
            responseEntity =  ResponseEntity.status(HttpStatus.OK).body("User was successfully created.");
        }
        catch (IllegalArgumentException | HibernateException e) {
            responseEntity =  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return responseEntity;
    }

    /**
     * Rest endpoint for creating a new booking
     * @param requestBody
     * @return
     */
    @PostMapping (value = "/create_booking")
    public ResponseEntity<String> createBooking(@RequestBody Map<String, String> requestBody) {

        //VALIDATE NAMES AND EMAIL
        final String firstName = requestBody.get("firstName");
        final String lastName = requestBody.get("lastName");
        final String email = requestBody.get("email");

        final String arrivalDateString = requestBody.get("arrivalDateString");
        final String departureDateString = requestBody.get("departureDateString");

        ResponseEntity<String> responseEntity;
        try {
            return  ResponseEntity.status(HttpStatus.OK).body("Booking Reference: " + campsiteService.attemptToCreateBooking(firstName, lastName, email, arrivalDateString, departureDateString));
        }
        catch (DateTimeParseException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please provide a valid format for the arrival and departure dates (YYYY-MM-DD).");
        }
        catch (HibernateException | IllegalArgumentException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return responseEntity;
    }

    /**
     * Rest endpoint for getting a string of available dates
     * @param arrivalDateString
     * @param departureDateString
     * @return
     */
    @GetMapping(value = "/availability")
    public ResponseEntity<String> getCampsiteAvailability(@RequestParam(value = "arrivalDateString", required = false) String arrivalDateString,  @RequestParam(value = "departureDateString", required = false) String departureDateString) {
        ResponseEntity<String> responseEntity;
        try {
            String availableDates = campsiteService.getBookingAvailability(arrivalDateString, departureDateString);
            return  ResponseEntity.status(HttpStatus.OK).body(availableDates);
        }
        catch (DateTimeParseException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please provide a valid format for the arrival and departure dates (YYYY-MM-DD).");
        }
        catch (HibernateException | IllegalArgumentException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return responseEntity;
    }

    @PutMapping (value = "/update_booking")
    public ResponseEntity<String> updateBooking(@RequestBody Map<String, String> requestBody) {
        ResponseEntity<String> responseEntity;

        final String bookingIdString =  requestBody.get("bookingId");
        final String arrivalDateString = requestBody.get("arrivalDateString");
        final String departureDateString = requestBody.get("departureDateString");

        try {
            campsiteService.attemptToUpdateBooking(bookingIdString, arrivalDateString, departureDateString);
            responseEntity = ResponseEntity.status(HttpStatus.OK).body("");
        }
        catch (DateTimeParseException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please provide a valid format for the arrival and departure dates (YYYY-MM-DD).");
        }
        catch (NumberFormatException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please provide a valid number format for the booking reference.");
        }
        catch (HibernateException | IllegalArgumentException | NullPointerException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return responseEntity;
    }
}
