package com.akash.campsite.controllers;

import com.akash.campsite.pojo.User;
import com.akash.campsite.service.CampsiteService;
import javassist.NotFoundException;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeParseException;
import java.util.Map;

import static com.akash.campsite.utility.CampsiteMessagesUtil.*;

/**
 * Created by Kash on 9/22/2018.
 */

@RestController
@RequestMapping(value = "/campsite")
public class CampsiteRestController {

    @Autowired
    private CampsiteService campsiteService;

    /**
     * Rest endpoint for creating a new user
     * @param user
     * @return
     */
    @PostMapping (value = "/user")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        ResponseEntity<String> responseEntity;
        try {
            campsiteService.attemptToCreateUser(user.getFirstName(), user.getLastName(), user.getEmail());
            responseEntity =  ResponseEntity.status(HttpStatus.CREATED).body(USER_CREATE_SUCCESS);
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
    @PostMapping (value = "/booking")
    public ResponseEntity<String> createBooking(@RequestBody Map<String, String> requestBody) {

        final String firstName = requestBody.get("firstName");
        final String lastName = requestBody.get("lastName");
        final String email = requestBody.get("email");

        final String arrivalDateString = requestBody.get("arrivalDateString");
        final String departureDateString = requestBody.get("departureDateString");

        ResponseEntity<String> responseEntity;
        try {
            return  ResponseEntity.status(HttpStatus.CREATED).body(BOOKING_SUCCESS + campsiteService.attemptToCreateBooking(firstName, lastName, email, arrivalDateString, departureDateString));
        }
        catch (DateTimeParseException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DATE_ERROR_FORMAT);
        }
        catch (HibernateException | IllegalArgumentException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return responseEntity;
    }

    /**
     * Rest endpoint for cancelling a booking.
     * @param bookingId
     * @return
     */
    @DeleteMapping (value = "/booking/{bookingId}")
    public ResponseEntity<String> deleteBooking(@PathVariable int bookingId ) {
        ResponseEntity<String> responseEntity;
        try {
            campsiteService.attemptToDeleteBooking(bookingId);
            responseEntity = ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
        }
        catch (HibernateException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (NotFoundException e) {
            responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
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
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DATE_ERROR_FORMAT);
        }
        catch (HibernateException | IllegalArgumentException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return responseEntity;
    }

    @PutMapping (value = "/booking/{bookingId}")
    public ResponseEntity<String> updateBooking(@PathVariable int bookingId, @RequestBody Map<String, String> requestBody) {
        ResponseEntity<String> responseEntity;

        //final String bookingIdString =  requestBody.get("bookingId");
        final String arrivalDateString = requestBody.get("arrivalDateString");
        final String departureDateString = requestBody.get("departureDateString");

        try {
            campsiteService.attemptToUpdateBooking(bookingId, arrivalDateString, departureDateString);
            responseEntity = ResponseEntity.status(HttpStatus.OK).body("");
        }
        catch (DateTimeParseException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DATE_ERROR_FORMAT);
        }
        catch (HibernateException | IllegalArgumentException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (NotFoundException e) {
            responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return responseEntity;
    }
}
