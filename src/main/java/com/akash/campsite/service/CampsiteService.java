package com.akash.campsite.service;

import com.akash.campsite.dao.CampsiteDAO;
import javassist.NotFoundException;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akash.campsite.pojo.Booking;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Created by Kash on 9/23/2018.
 */
@Service
public class CampsiteService {

    @Autowired
    private CampsiteDAO campsiteDAO;

    /**
     *
     * @param firstName
     * @param lastName
     * @param email
     * @param arrivalDateString
     * @param departureDateString
     * @return
     * @throws HibernateException
     * @throws IllegalArgumentException
     */
    public int attemptToCreateBooking(final String firstName, final String lastName, final String email, final String arrivalDateString, final String departureDateString) throws HibernateException, IllegalArgumentException {

        LocalDate arrivalDate = parseDateString(arrivalDateString);
        LocalDate departureDate = parseDateString(departureDateString);

        if (arrivalDate == null || departureDate == null) {
            throw new IllegalArgumentException("Invalid date range: The arrival date and the departure date need to be provided in order to create a booking.");
        }

        validateBoookingDateRange(arrivalDate, departureDate);

        int userId = campsiteDAO.searchUserByEmail(email);

        // Check if user exists
        if (userId == -1) {
            userId = campsiteDAO.createUser(firstName, lastName, email);
        }

        return campsiteDAO.createBooking(userId, arrivalDate, departureDate);
    }

    /**
     * Attempts to create a new user in the database by first making sure that a User with the same
     * email does not already exist.
     *
     * @param firstName     user's first name
     * @param lastName      user's last name
     * @param email         user's email
     */
    public void attemptToCreateUser(final String firstName, final String lastName, final String email) throws HibernateException{

        // Make sure user does not exist
        if (campsiteDAO.searchUserByEmail(email) == -1) {
            campsiteDAO.createUser(firstName, lastName, email);
        }
        else {
            throw new IllegalArgumentException("A user with the provided email already exists: " + email);
        }
    }

    /**
     * Attempts to delete a booking in the database  by first making sure that the booking exists.
     * In the case it does not, an IllegalArgumentException is propagated to the controller to return an appropriate
     * message in the response.
     *
     * @param bookingId             bookingId of the booking to delete
     * @throws HibernateException   Propagated to the controller to return an appropriate message in case an error occurs
     */
    public void attemptToDeleteBooking(final int bookingId) throws HibernateException, NotFoundException {

        if (bookingId > 0 && campsiteDAO.searchBookingById(bookingId)) {
            campsiteDAO.cancelBooking(bookingId);
        }
        else {
            throw new NotFoundException("Cannot cancel the booking as it does not exist.");
        }
    }

    /**
     *
     * @param bookingId
     * @param arrivalDateString
     * @param departureDateString
     * @throws DateTimeParseException
     * @throws HibernateException
     * @throws NotFoundException
     */
    public void attemptToUpdateBooking(final int bookingId, final String arrivalDateString, final String departureDateString) throws DateTimeParseException, HibernateException, NotFoundException {

        if (bookingId > 0 && campsiteDAO.searchBookingById(bookingId)) {
            LocalDate arrivalDate = parseDateString(arrivalDateString);
            LocalDate departureDate = parseDateString(departureDateString);

            if (arrivalDate == null || departureDate == null) {
                throw new IllegalArgumentException("Invalid date range: The arrival date and the departure date need to be provided in order to update a booking.");
            }

            validateBoookingDateRange(arrivalDate, departureDate);
            campsiteDAO.updateBooking(bookingId, arrivalDate, departureDate);
        }
        else {
            throw new NotFoundException("Cannot update the booking as it does not exist.");
        }
    }

    /**
     * Returns a comma separated string with dates (YYYY-MM-DD) the campsite can be booked on, based on
     * the date range given.
     *
     * @param arrivalDateString             String representation of the beginning of the date range
     * @param departureDateString           String representation of the end of the date range
     *
     * @return                              Comma separated string with dates (YYYY-MM-DD) the campsite can be booked on,
     *
     * @throws DateTimeParseException       Propagate the exception to the controller if an error occurred while parsing the string representations of the dates
     * @throws HibernateException           Propagate the exception to the controller if an error occurred with the database so that
     *                                      an appropriate message can be sent back.
     */
    public String getBookingAvailability(final String arrivalDateString, final String departureDateString) throws DateTimeParseException, HibernateException {
        LocalDate arrivalDate = parseDateString(arrivalDateString);
        LocalDate departureDate = parseDateString(departureDateString);

        // Need to validate the dates and make sure they are not null. Cannot put this in a separate method as Java is pass
        // by value (a new reference would be passed to the method).
        boolean needToValidateArrivalDate = true;
        boolean needToValidateDepartureDate = true;

        if (arrivalDate == null) {
            // Since the user needs to book at a minimum of 1 day in advance, the arrival date should start 1 day in the future if it was not provided
            arrivalDate = LocalDate.now().plusDays(1);
            needToValidateArrivalDate = false;
        }

        if (departureDate == null) {
            // Since the user can book at most 1 month in the future. -1 day to stay within a month from the current date
            departureDate = arrivalDate.plusMonths(1).plusDays(-1);
            needToValidateDepartureDate = false;
        }

        if (needToValidateArrivalDate || needToValidateDepartureDate) {
            validateDateRange(arrivalDate, departureDate);
        }

        final List<Booking> bookings = campsiteDAO.getBookingsInDateRange(arrivalDate, departureDate);
        final List<String> availableDates = new ArrayList<>();
        final Set<String> takenDates = new HashSet<>();

        for (Booking b: bookings) {
            final LocalDate bookingArrival = b.getArrivalDate();
            final  LocalDate bookingDeparture = b.getDepartureDate();

            LocalDate date = LocalDate.of(bookingArrival.getYear(), bookingArrival.getMonth(), bookingArrival.getDayOfMonth());

            // Get Set of takenDates
            while (date.isBefore(bookingDeparture)) {
                if (date.isEqual(bookingArrival) || date.isAfter(bookingArrival)) {
                    takenDates.add(date.toString());
                }
                date = date.plusDays(1);
            }
        }

        // Get set of available dates
        LocalDate iteratingDate = LocalDate.of(arrivalDate.getYear(), arrivalDate.getMonth(), arrivalDate.getDayOfMonth());
        while (iteratingDate.isBefore(departureDate) || iteratingDate.equals(departureDate)) {
            if (!takenDates.contains(iteratingDate.toString())) {
                availableDates.add(iteratingDate.toString());
            }
            iteratingDate = iteratingDate.plusDays(1);
        }

        // Sort the List of dates
        availableDates.sort(new Comparator<String>() {
            DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD");

            @Override
            public int compare(String date1, String date2) {
                try {
                    return dateFormat.parse(date1).compareTo(dateFormat.parse(date2));
                } catch (ParseException e) {
                   e.printStackTrace();
                }
                return 0;
            }
        });

        return availableDates.toString();
    }

    /**
     * Attempts to parse the dateString (YYYY-MM-DD) into a LocalDate object. In case
     * dateString is null or empty, the date is set to the current date.
     *
     * If parsing is not possible, an exception is propagated upwards
     * where it will be handled.
     *
     * @param dateString
     * @return
     * @throws DateTimeParseException
     */
    private LocalDate parseDateString(final String dateString) throws DateTimeParseException {
        LocalDate date = null;
        if (dateString != null && !dateString.isEmpty()) {
            date = LocalDate.parse(dateString);
        }
        return date;
    }

    /**
     * Calls validateDateRange to verify that date range is valid and then verifies that the user is not attempting
     * to book the campsite for more than 3 days.
     *
     * @param arrivalDate       Beginning on the date range
     * @param departureDate     End of the date range
     */
    private void validateBoookingDateRange(final LocalDate arrivalDate, final LocalDate departureDate) throws IllegalArgumentException {
        validateDateRange(arrivalDate, departureDate);

        // Make sure the user is trying to book for a max of 3 days
        LocalDate tempDate = LocalDate.of(arrivalDate.getYear(), arrivalDate.getMonth(), arrivalDate.getDayOfMonth());
        int numberOfDays = 0;

        // Get number of days between date range
        while (tempDate.isBefore(departureDate)) {
            tempDate = tempDate.plusDays(1);
            numberOfDays++;
        }

        if (numberOfDays > 3) {
            throw new IllegalArgumentException("Invalid date range: The campsite cannot be booked for more than 3 days.");
        }
    }

    /**
     * Validates the date range by making sure the following criteria are met:
     *      -arrivalDate is before the departureDate
     *      -arrivalDate is at least 1 day in advance
     *      -date range is not in the past
     *
     * Then calls validateDateRangeMonthConstraint to validate the 1 month constraint.
     *
     * @param arrivalDate           Beginning on the date range
     * @param departureDate         End of the date range
     */
    private void validateDateRange(LocalDate arrivalDate, LocalDate departureDate) throws IllegalArgumentException {
        LocalDate currentDate = LocalDate.now();

        // Make sure the arrivalDate is before the departure date
        if (arrivalDate.isBefore(departureDate)) {

            // Verify that the arrivalDate is at least 1 day in advance
            if (currentDate.isBefore(arrivalDate)) {

                validateDateRangeMonthConstraint(arrivalDate, departureDate);
            }
            else {
                throw new IllegalArgumentException("Invalid date range: The arrival date must be at least 1 day in advance.");
            }
        }
        else {
            throw new IllegalArgumentException("Invalid date range: The arrival date must be before the departure date.");
        }
    }

    /**
     * Validates that the date range is within 1 month from today
     *
     * @param arrivalDate           Beginning on the date range
     * @param departureDate         End of the date range
     */
    private void  validateDateRangeMonthConstraint(final LocalDate arrivalDate, final LocalDate departureDate) {
        // Make sure the availability shown is for at most 1 month in the future
        LocalDate futureDate = LocalDate.now().plusMonths(1);

        if (arrivalDate.isAfter(futureDate) || departureDate.isAfter(futureDate)) {
            throw new IllegalArgumentException("Invalid date range: The arrival date and the departure date cannot be more than 1 month in the future.");
        }
    }
}
