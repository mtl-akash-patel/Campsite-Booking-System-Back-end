package com.akash.campsite.dao;

import com.akash.campsite.pojo.Booking;
import com.akash.campsite.pojo.User;
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

/**
 * Created by Kash on 9/25/2018.
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
     * Deletes the booking with the matching bookingId from the database
     *
     * @param bookingId     bookingId of the booking to delete
     */
    public void cancelBooking(final int bookingId){
        final Session session = factory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            Booking booking = (Booking)session.get(Booking.class, bookingId);
            session.delete(booking);
            transaction.commit();
        }
        catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new HibernateException("An error occurred while attempting to cancel the booking. Please try again");
        } finally {
            session.close();
        }
    }


    /**
     * Creates a new Booking in the database and returns the bookingId
     *
     * @param userId            Id of the User that created the booking
     * @param arrivalDate       Beginning on the date range
     * @param departureDate     End of the date range
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
            throw new HibernateException("An error occurred while attempting to create the booking. Please make sure the campsite is available on the given dates and try again.");
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
     * @return              The id of the newly created user
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
            throw new HibernateException("An error occurred while trying to create the user. Please try again.");

        } finally {
            session.close();
        }
        return userId;
    }

    /**
     * Queries the database to get a list of Booking objects within the date range.
     *
     * @param arrivalDate       Beginning on the date range
     * @param departureDate     End of the date range
     * @return                  List of Booking objects with dates within the range
     */
    public List<Booking> getBookingsInDateRange(final LocalDate arrivalDate, final LocalDate departureDate) {
        final Session session = factory.openSession();
        final List<Booking> results;
        try {
            //final String hql = "FROM Booking b WHERE (b.arrivalDate >= :arrivalDate AND b.departureDate < :departureDate";
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
            throw new HibernateException("An error occurred while trying to retrieve the available dates. Please try again.");
        } finally {
            session.close();
        }
        return results;
    }

    /**
     * Returns a boolean value indicating whether a booking with the provided bookingId exists
     *
     * @param bookingId     bookingId to search for
     * @return              boolean indicating if the booking was found
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
            throw new HibernateException("An error occurred while trying to search for the booking. Please try again.");
        } finally {
            session.close();
        }
        return found;
    }

    /**
     * Returns a userId or -1 depending on whether the user with the
     * specified email (unique) exists.
     *
     * @param email     User's email
     * @return          userId if the user exists. Else, -1
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
            throw new HibernateException("An error occurred while trying to search for the user. Please try again.");
        } finally {
            session.close();
        }
        return id;
    }

    /**
     *
     * @param bookingId
     * @param arrivalDate
     * @param departureDate
     */
    public void updateBooking(final int bookingId, final LocalDate arrivalDate, final LocalDate departureDate) {
        final Session session = factory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            Booking booking = (Booking)session.get(Booking.class, bookingId);
            if (booking == null) {
                throw new NullPointerException("Cannot update the booking as it does not exist.");
            }

            booking.setArrivalDate(arrivalDate);
            booking.setDepartureDate(departureDate);
            session.update(booking);
            transaction.commit();
        }
        catch (PersistenceException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new HibernateException("An error occurred while attempting to update the booking. Please make sure the campsite is available on the given dates and try again.");
        } finally {
            session.close();
        }
    }
}
