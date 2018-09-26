package com.akash.campsite.service;

import com.akash.campsite.dao.CampsiteDAO;
import com.akash.campsite.pojo.Booking;
import javassist.NotFoundException;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

import static com.akash.campsite.utility.CampsiteMessagesUtil.*;

/**
 * Created by Kash on 9/23/2018.
 */
@Service
public class CampsiteService {

    @Autowired
    private CampsiteDAO campsiteDAO;

    /**
     * Attempts to create a new Booking after calling other methods to perform the required validation. A new User
     * is also created if the User that wishes to create the Booking does not exist yet.
     *
     * @param firstName                     User's first name
     * @param lastName                      User's last name
     * @param email                         User's email
     * @param arrivalDateString             String representation of the arrival date the user wishes to create a Booking for
     * @param departureDateString           String representation of the departure date the user wishes to create a Booking for
     *
     * @return                              Returns the bookingId of the Booking if it is created successfully
     *
     * @throws DateTimeParseException       Thrown when the string representation of a date is in an invalid format and cannot be parsed
     * @throws HibernateException           Thrown when an error occurs at the database level
     * @throws IllegalArgumentException     Thrown when validating the user fields or the dates fails
     */
    public int attemptToCreateBooking(final String firstName, final String lastName, final String email, final String arrivalDateString, final String departureDateString) throws DateTimeParseException, HibernateException, IllegalArgumentException {

        if (!validateString(firstName) || !validateString(lastName) || !validateString(email)) {
            throw new IllegalArgumentException(USER_ERROR_NOT_PROVIDED);
        }

        LocalDate arrivalDate = parseDateString(arrivalDateString);
        LocalDate departureDate = parseDateString(departureDateString);

        if (arrivalDate == null || departureDate == null) {
            throw new IllegalArgumentException(DATE_ERROR_NOT_PROVIDED);
        }

        validateBookingDateRange(arrivalDate, departureDate);

        int userId = campsiteDAO.searchUserByEmail(email);

        // Check if User exists
        if (userId == -1) {
            userId = campsiteDAO.createUser(firstName, lastName, email);
        }

        return campsiteDAO.createBooking(userId, arrivalDate, departureDate);
    }

    /**
     * Attempts to create a new User in the database by validating the user data and by first making sure that a User with the same
     * email does not already exist. An IllegalArgumentException is thrown if User data is not provided or if a User with the same
     * email already exists.
     *
     * @param firstName             User's first name
     * @param lastName              User's last name
     * @param email                 User's email
     *
     * @throws HibernateException   Thrown when an error occurs at the database level
     */
    public void attemptToCreateUser(final String firstName, final String lastName, final String email) throws HibernateException{

        if (!validateString(firstName) || !validateString(lastName) || !validateString(email)) {
            throw new IllegalArgumentException(USER_ERROR_NOT_PROVIDED);
        }

        // Make sure User does not exist
        if (campsiteDAO.searchUserByEmail(email) == -1) {
            campsiteDAO.createUser(firstName, lastName, email);
        }
        else {
            throw new IllegalArgumentException(USER_ERROR_ALREADY_EXISTS + email);
        }
    }

    /**
     * Attempts to delete a Booking in the database
     *
     * @param bookingId             bookingId of the Booking to delete
     *
     * @throws HibernateException       Thrown when an error occurs at the database level
     * @throws IllegalArgumentException Thrown when there is an attempt to cancel a past booking
     * @throws NotFoundException        Thrown when the Booking does not exist
     */
    public void attemptToDeleteBooking(final int bookingId) throws HibernateException, NotFoundException, IllegalArgumentException {
        campsiteDAO.cancelBooking(bookingId);
    }

    /**
     * Attempts to update a Booking after calling other methods to perform the required validation.
     *
     * @param bookingId                 bookingId of thw Booking to update
     * @param arrivalDateString         String representation of the Booking's new arrival date
     * @param departureDateString       String representation of the Booking's new departure date
     *
     * @throws DateTimeParseException   Thrown when the string representation of a date is in an invalid format and cannot be parsed
     * @throws IllegalArgumentException Thrown when validating the dates fails
     * @throws HibernateException       Thrown when an error occurs at the database level
     * @throws NotFoundException        Thrown when the Booking does not exist
     */
    public void attemptToUpdateBooking(final int bookingId, final String arrivalDateString, final String departureDateString) throws DateTimeParseException, HibernateException, NotFoundException, IllegalArgumentException {
            LocalDate arrivalDate = parseDateString(arrivalDateString);
            LocalDate departureDate = parseDateString(departureDateString);

            if (arrivalDate == null || departureDate == null) {
                throw new IllegalArgumentException(DATE_ERROR_NOT_PROVIDED);
            }

            validateBookingDateRange(arrivalDate, departureDate);
            campsiteDAO.updateBooking(bookingId, arrivalDate, departureDate);
    }

    /**
     * Returns a comma separated string with dates (YYYY-MM-DD) the campsite can be booked on, based on
     * the date range given.
     *
     * @param arrivalDateString         String representation of the beginning of the date range
     * @param departureDateString       String representation of the end of the date range
     *
     * @return                          Sorted comma separated string with dates (YYYY-MM-DD) the campsite can be booked on,
     *
     * @throws DateTimeParseException   Thrown when the string representation of a date is in an invalid format and cannot be parsed
     * @throws IllegalArgumentException Thrown when validating the dates fails
     * @throws HibernateException       Thrown when an error occurs at the database level
     */
    public String getBookingAvailability(final String arrivalDateString, final String departureDateString) throws DateTimeParseException, HibernateException, IllegalArgumentException {
        LocalDate arrivalDate = parseDateString(arrivalDateString);
        LocalDate departureDate = parseDateString(departureDateString);

        // Need to validate the dates and make sure they are not null. Cannot put this in a separate method as Java is pass
        // by value (a new reference would be passed to the method and making that new reference point to a new LocalDate would not affect the LocalDate variable
        // in this method).
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

        // Using a List instead of a Set for availableDates so that sorting it is easier
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

        // Get List of available dates
        LocalDate iteratingDate = LocalDate.of(arrivalDate.getYear(), arrivalDate.getMonth(), arrivalDate.getDayOfMonth());
        while (iteratingDate.isBefore(departureDate) || iteratingDate.equals(departureDate)) {
            if (!takenDates.contains(iteratingDate.toString())) {
                availableDates.add(iteratingDate.toString());
            }
            iteratingDate = iteratingDate.plusDays(1);
        }

        // Sort the List of dates.
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
     * Attempts to parse the dateString (YYYY-MM-DD) into a LocalDate object.
     *
     * @param dateString                String representation of the date to parse
     *
     * @return                          Null or LocalDate object if the dateString was successfully parsed
     *
     * @throws DateTimeParseException   Thrown when parsing the dateString is not possible
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
     * to book the campsite for more than 3 days. Throws IllegalArgumentException if the User is attempting to book for
     * more than 3 days.
     *
     * @param arrivalDate               Beginning on the date range
     * @param departureDate             End of the date range
     *
     * @throws IllegalArgumentException Thrown when the date range is not valid
     */
    private void validateBookingDateRange(final LocalDate arrivalDate, final LocalDate departureDate) throws IllegalArgumentException {
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
            throw new IllegalArgumentException(DATE_ERROR_RANGE_3_DAYS);
        }
    }

    /**
     * Validates the date range by making sure the following criteria are met:
     *      -arrivalDate is before the departureDate
     *      -arrivalDate is at least 1 day in advance
     *      -date range is not in the past
     *
     * Then calls validateDateRangeMonthConstraint to validate the 1 month constraint.
     * IllegalArgumentException is thrown when the constraints are not met.
     *
     * @param arrivalDate               Beginning on the date range
     * @param departureDate             End of the date range
     *
     * @throws IllegalArgumentException Thrown when the date range is not valid
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
                throw new IllegalArgumentException(DATE_ERROR_RANGE_1_DAY_ADVANCE);
            }
        }
        else {
            throw new IllegalArgumentException(DATE_ERROR_RANGE_DEPARTURE_BEFORE_ARRIVAL);
        }
    }

    /**
     * Validates that the date range is within 1 month from today. In case it is not, an IllegalArgumentException is thrown.
     *
     * @param arrivalDate           Beginning on the date range
     * @param departureDate         End of the date range
     */
    private void validateDateRangeMonthConstraint(final LocalDate arrivalDate, final LocalDate departureDate) {
        // Make sure the availability shown is for at most 1 month in the future
        LocalDate futureDate = LocalDate.now().plusMonths(1);

        if (arrivalDate.isAfter(futureDate) || departureDate.isAfter(futureDate)) {
            throw new IllegalArgumentException(DATE_ERROR_RANGE_1_MONTH_ADVANCE);
        }
    }

    /**
     * Validates a string by checking if it is null or empty.
     *
     * @param s String to validate
     *
     * @return  Boolean indicating whether the String is valid
     */
    private boolean validateString(final String s) {
        return !(s == null || s.trim().isEmpty());
    }
}
