package com.akash.campsite.utility;

/**
 * Created by Kash on 9/26/2018.
 *
 * Class used to hold the messages sent back to the caller. Ideally the messages should be
 * read from a resource bundle for internationalization rather than being constant strings.
 */
public class CampsiteMessagesUtil {

    public static final String AVAILABILITY_ERROR_HIBERNATE = "An error occurred while trying to retrieve the available dates, please try again.";

    public static final String BOOKING_ERROR_CANCEL_HIBERNATE = "An error occurred while attempting to cancel the booking, please try again. Booking Reference: ";

    public static final String BOOKING_ERROR_CANCEL_NON_EXISTENT = "Cannot cancel the booking as it does not exist: ";

    public static final String BOOKING_ERROR_CREATE_HIBERNATE = "An error occurred while attempting to create the booking. Please make sure the campsite is available on the given dates and try again.";

    public static final String BOOKING_ERROR_SEARCH_HIBERNATE = "An error occurred while trying to search for the booking, please try again. Booking Reference: ";

    public static final String BOOKING_ERROR_UPDATE_HIBERNATE = "An error occurred while attempting to update the booking. Please make sure the campsite is available on the given dates and try again. Booking Reference: ";

    public static final String BOOKING_ERROR_UPDATE_NON_EXISTENT = "Cannot update the booking as it does not exist: ";

    public static final String BOOKING_SUCCESS = "Booking Reference: ";

    public static final String DATE_ERROR_FORMAT = "Please provide a valid format for the arrival and departure dates (YYYY-MM-DD).";

    public static final String DATE_ERROR_NOT_PROVIDED = "Invalid date range: The arrival date and the departure date need to be provided in order to create a booking.";

    public static final String DATE_ERROR_RANGE_1_DAY_ADVANCE = "Invalid date range: The arrival date must be at least 1 day in advance.";

    public static final String DATE_ERROR_RANGE_1_MONTH_ADVANCE = "Invalid date range: The arrival date and the departure date cannot be more than 1 month in the future.";

    public static final String DATE_ERROR_RANGE_3_DAYS = "Invalid date range: The campsite cannot be booked for more than 3 days.";

    public static final String DATE_ERROR_RANGE_DEPARTURE_BEFORE_ARRIVAL = "Invalid date range: The arrival date must be before the departure date.";

    public static final String USER_CREATE_ERROR_HIBERNATE = "An error occurred while trying to create the user, please try again.";

    public static final String USER_CREATE_SUCCESS = "User was successfully created";

    public static final String USER_ERROR_ALREADY_EXISTS = "A user with the provided email already exists: ";

    public static final String USER_ERROR_NOT_PROVIDED = "The user's first name, last name, and email must be provided.";

    public static final String USER_ERROR_SEARCH_HIBERNATE = "An error occurred while trying to search for the user, please try again. User email: ";
}
