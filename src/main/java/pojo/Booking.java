package pojo;

import java.time.LocalDate;

/**
 * Created by Kash on 9/23/2018.
 */
public class Booking {

    private int bookingId;
    private int userId;
    private LocalDate arrivalDate;
    private LocalDate departureDate;

    public Booking() {}

    public Booking(int userId, LocalDate arrivalDate, LocalDate departureDate) {
        this.userId = userId;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }
}
