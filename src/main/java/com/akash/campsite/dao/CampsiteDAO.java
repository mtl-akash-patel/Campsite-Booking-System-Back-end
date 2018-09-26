package com.akash.campsite.dao;

import com.akash.campsite.pojo.Booking;
import com.akash.campsite.pojo.User;
import javassist.NotFoundException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.List;

import static com.akash.campsite.utility.CampsiteMessagesUtil.*;

/**
 * Created by Kash on 9/25/2018.
 *
 * DAO class for User and Booking entities. HibernateExceptions are caught
 * in case an error occurs at the database level. The exceptions are then rethrown
 * with appropriate error messages so that they can be caught by the controller
 * to send a response with the appropriate response code and error message.
 */

@Repository
public class CampsiteDAO {

    private SessionFactory factory;

    public CampsiteDAO() {
        try {
            factory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Deletes the Booking with the matching bookingId from the database.
     * NotFoundException is thrown if the Booking does not exist. IllegalArgumentException
     * is thrown when there is an attempt to cancel a past booking.
     *
     * @param bookingId             bookingId of the Booking to delete
     *
     * @throws NotFoundException    Thrown if the Booking does not exist
     */
    public void cancelBooking(final int bookingId) throws NotFoundException {
        final Session session = factory.openSession();
        Transaction transaction = null;

        if (bookingId > 0 && searchBookingById(bookingId)) {
            try {
                transaction = session.beginTransaction();
                Booking booking = (Booking) session.get(Booking.class, bookingId);
                session.delete(booking);
                transaction.commit();
            }
            catch (HibernateException e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                e.printStackTrace();
                throw new HibernateException(BOOKING_ERROR_CANCEL_HIBERNATE + bookingId);
            }
            catch (PersistenceException e) {
                // Caught when the user attempts to delete a past Booking
                e.printStackTrace();
               throw new IllegalArgumentException(BOOKING_ERROR_CANCEL_PAST + bookingId);
            } finally {
                session.close();
            }
        }
        else {
            throw new NotFoundException(BOOKING_ERROR_CANCEL_NON_EXISTENT + bookingId);
        }
    }

    /**
     * Creates a new Booking in the database and returns the bookingId
     *
     * @param userId            Id of the User that created the Booking
     * @param arrivalDate       Booking arrival date
     * @param departureDate     Booking departure date
     *
     * @return                  bookingId of the newly created Booking
     */
    public int createBooking(final int userId, final LocalDate arrivalDate, final LocalDate departureDate) {
        Transaction transaction = null;
        int bookingId = -1;

        final Session session = factory.openSession();
        try {
            transaction = session.beginTransaction();
            Booking booking = new Booking(userId, arrivalDate, departureDate);
            bookingId = (Integer) session.save(booking);
            transaction.commit();
        }
        catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new HibernateException(BOOKING_ERROR_CREATE_HIBERNATE);
        } finally {
            session.close();
        }
        return bookingId;
    }

    /**
     * Creates a new User in the database and returns the userId
     *
     * @param firstName     User's first name
     * @param lastName      User's last name
     * @param email         User's email
     *
     * @return              userId of the newly created User
     */
    public int createUser (final String firstName, final String lastName, final String email) {
        Transaction transaction = null;
        int userId = -1;

        final Session session = factory.openSession();
        try {
            transaction = session.beginTransaction();
            final User user = new User(firstName, lastName, email);
            userId = (Integer) session.save(user);
            transaction.commit();
        }
        catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new HibernateException(USER_CREATE_ERROR_HIBERNATE);

        } finally {
            session.close();
        }
        return userId;
    }

    /**
     * Queries the database to get a list of Booking objects within the date range.
     *
     * @param arrivalDate       Beginning of the date range query
     * @param departureDate     End of the date range query
     *
     * @return                  List of Booking objects with dates within the range
     */
    public List<Booking> getBookingsInDateRange(final LocalDate arrivalDate, final LocalDate departureDate) {
        final Session session = factory.openSession();
        final List<Booking> results;
        try {
            final String hql = "FROM Booking b WHERE (b.arrivalDate <= :arrivalDate AND :arrivalDate <= b.departureDate) OR " +
                    "(:arrivalDate <= b.arrivalDate AND b.arrivalDate <= :departureDate) OR " +
                    "(:arrivalDate <= b.arrivalDate AND :departureDate >= b.departureDate) OR" +
                    "(:arrivalDate >= b.arrivalDate AND :departureDate <= b.departureDate)";
            final Query query = session.createQuery(hql);
            query.setParameter("arrivalDate", arrivalDate);
            query.setParameter("departureDate", departureDate);
            results = (List<Booking>) query.list();
        }
        catch (HibernateException e) {
            e.printStackTrace();
            throw new HibernateException(AVAILABILITY_ERROR_HIBERNATE);
        } finally {
            session.close();
        }
        return results;
    }

    /**
     * Returns a boolean value indicating whether a Booking with the provided bookingId exists
     *
     * @param bookingId     bookingId to search for
     *
     * @return              boolean indicating if the Booking was found
     */
    public boolean searchBookingById(final int bookingId) {
        boolean found = false;
        final Session session = factory.openSession();

        final List<Integer> results;
        try {
            final String hql = "SELECT b.bookingId FROM Booking b WHERE b.bookingId = :bookingId";
            final Query query = session.createQuery(hql);
            query.setParameter("bookingId", bookingId);
            results = (List<Integer>) query.list();

            if (results != null && !results.isEmpty()) {
                found = true;
            }
        }
        catch (HibernateException e) {
            e.printStackTrace();
            throw new HibernateException(BOOKING_ERROR_SEARCH_HIBERNATE + bookingId);
        } finally {
            session.close();
        }
        return found;
    }

    /**
     * Returns a userId or -1 depending on whether the User with the
     * specified email (unique) exists.
     *
     * @param email     User's email
     *
     * @return          userId if the User exists. Else, -1
     */
    public int searchUserByEmail(final String email) {
        final Session session = factory.openSession();
        int id = -1;
        final List<Integer> results;
        try {
            final String hql = "SELECT u.userId FROM User u WHERE u.email = :email";
            final Query query = session.createQuery(hql);
            query.setParameter("email", email);
            results = (List<Integer>) query.list();

            if (results != null && !results.isEmpty()) {
                id = results.get(0);
            }
        }
        catch (HibernateException e) {
            e.printStackTrace();
            throw new HibernateException(USER_ERROR_SEARCH_HIBERNATE + email);
        } finally {
            session.close();
        }
        return id;
    }

    /**
     *  Attempts to update a Booking with a new arrival date and a new departure date.
     *  A PersistenceException is caught if the new dates are within the range of an existing Booking's dates,
     *  A NotFoundException is thrown if the Booking does not exist.
     *
     * @param bookingId             bookingId of the Booking to update
     * @param arrivalDate           New arrivalDate to update
     * @param departureDate         New departureDate to update
     *
     * @throws NotFoundException    Thrown if the Booking does not exist
     */
    public void updateBooking(final int bookingId, final LocalDate arrivalDate, final LocalDate departureDate) throws NotFoundException {
        final Session session = factory.openSession();
        Transaction transaction = null;

        if (bookingId > 0 && searchBookingById(bookingId)) {
            try {
                transaction = session.beginTransaction();
                Booking booking = (Booking) session.get(Booking.class, bookingId);

                if (booking == null) {
                    throw new NotFoundException(BOOKING_ERROR_UPDATE_NON_EXISTENT);
                }

                booking.setArrivalDate(arrivalDate);
                booking.setDepartureDate(departureDate);
                session.update(booking);
                transaction.commit();
            } catch (PersistenceException e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                e.printStackTrace();
                throw new HibernateException(BOOKING_ERROR_UPDATE_HIBERNATE + bookingId);
            } finally {
                session.close();
            }
        }
        else {
            throw new NotFoundException(BOOKING_ERROR_UPDATE_NON_EXISTENT + bookingId);
        }
    }
}
