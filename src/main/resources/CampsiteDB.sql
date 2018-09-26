DROP DATABASE IF EXISTS CAMPSITE;
CREATE DATABASE CAMPSITE;
USE CAMPSITE;

         DROP TABLE IF EXISTS USERS;
         CREATE TABLE USERS(
           USER_ID INT NOT NULL AUTO_INCREMENT,
           FIRST_NAME VARCHAR(50) NOT NULL,
           LAST_NAME VARCHAR(50) NOT NULL,
           EMAIL VARCHAR(50) NOT NULL UNIQUE,
           PRIMARY KEY(USER_ID)
         );

         DROP TABLE IF EXISTS BOOKINGS;
         CREATE TABLE BOOKINGS(
           BOOKING_ID INT NOT NULL AUTO_INCREMENT,
           USER_ID INT NOT NULL,
           ARRIVAL_DATE DATE NOT NULL UNIQUE,
           DEPARTURE_DATE DATE NOT NULL UNIQUE,
           PRIMARY KEY(BOOKING_ID),
           FOREIGN KEY(USER_ID) REFERENCES USERS( USER_ID)
         );

         #Triggers to make sure that the ARRIVAL_DATE is before or equal to the DEPARTURE_DATE and that booking is possible within the date range
         DELIMITER $$
         DROP TRIGGER IF EXISTS DATES_INSERT_TRIGGER
         $$
         CREATE TRIGGER DATES_INSERT_TRIGGER
         BEFORE INSERT ON BOOKINGS
         FOR EACH ROW
         BEGIN
            DECLARE case1Count INT;
            DECLARE case2Count INT;
            DECLARE case3Count INT;
            DECLARE case4Count INT;
            IF (NEW.ARRIVAL_DATE >= NEW.DEPARTURE_DATE) THEN
                SIGNAL SQLSTATE '45000'
                     SET MESSAGE_TEXT = 'The arrival date cannot be on or after the departure date';
            ELSE
                SET case1Count = (SELECT COUNT(*) FROM BOOKINGS WHERE ARRIVAL_DATE < NEW.ARRIVAL_DATE AND NEW.ARRIVAL_DATE < DEPARTURE_DATE);
                SET case2Count = (SELECT COUNT(*) FROM BOOKINGS WHERE NEW.ARRIVAL_DATE < ARRIVAL_DATE AND ARRIVAL_DATE < NEW.DEPARTURE_DATE);
                SET case3Count = (SELECT COUNT(*) FROM BOOKINGS WHERE NEW.ARRIVAL_DATE < ARRIVAL_DATE AND NEW.DEPARTURE_DATE > DEPARTURE_DATE);
                SET case4Count = (SELECT COUNT(*) FROM BOOKINGS WHERE NEW.ARRIVAL_DATE > ARRIVAL_DATE AND NEW.DEPARTURE_DATE < DEPARTURE_DATE);
                IF ((case1Count + case2Count + case3Count + case4Count) > 0) THEN
                    SIGNAL SQLSTATE '45000'
                        SET MESSAGE_TEXT = 'The campsite is already booked between the requested dates';
                END IF;
            END IF;
         END;
         $$

         DELIMITER $$
         DROP TRIGGER IF EXISTS DATES_UPDATE_TRIGGER_RANGE
         $$
         CREATE TRIGGER DATES_UPDATE_TRIGGER_RANGE
         BEFORE UPDATE ON BOOKINGS
         FOR EACH ROW
         BEGIN
            DECLARE case1Count INT;
            DECLARE case2Count INT;
            DECLARE case3Count INT;
            DECLARE case4Count INT;
            IF (NEW.ARRIVAL_DATE >= NEW.DEPARTURE_DATE) THEN
                SIGNAL SQLSTATE '45000'
                     SET MESSAGE_TEXT = 'The arrival date cannot be on or after the departure date';
            ELSE
                SET case1Count = (SELECT COUNT(*) FROM BOOKINGS WHERE NEW.BOOKING_ID != BOOKING_ID AND ARRIVAL_DATE < NEW.ARRIVAL_DATE AND NEW.ARRIVAL_DATE < DEPARTURE_DATE);
                SET case2Count = (SELECT COUNT(*) FROM BOOKINGS WHERE NEW.BOOKING_ID != BOOKING_ID AND NEW.ARRIVAL_DATE < ARRIVAL_DATE AND ARRIVAL_DATE < NEW.DEPARTURE_DATE);
                SET case3Count = (SELECT COUNT(*) FROM BOOKINGS WHERE NEW.BOOKING_ID != BOOKING_ID AND NEW.ARRIVAL_DATE < ARRIVAL_DATE AND NEW.DEPARTURE_DATE > DEPARTURE_DATE);
                SET case4Count = (SELECT COUNT(*) FROM BOOKINGS WHERE NEW.BOOKING_ID != BOOKING_ID AND NEW.ARRIVAL_DATE > ARRIVAL_DATE AND NEW.DEPARTURE_DATE < DEPARTURE_DATE);
                IF ((case1Count + case2Count + case3Count + case4Count) > 0) THEN
                    SIGNAL SQLSTATE '45000'
                        SET MESSAGE_TEXT = 'The campsite is already booked between the requested dates';
                END IF;
            END IF;
         END;
         $$

         # Trigger to disallow deleting bookings in the past
         DELIMITER $$
         DROP TRIGGER IF EXISTS BOOKING_CANCEL_TRIGGER
         $$
         CREATE TRIGGER BOOKING_CANCEL_TRIGGER
         BEFORE DELETE ON BOOKINGS
         FOR EACH ROW
         BEGIN
            IF (OLD.DEPARTURE_DATE < CURDATE()) THEN
                SIGNAL SQLSTATE '45000'
                     SET MESSAGE_TEXT = 'Cannot cancel a booking from the past';
            END IF;
         END;
         $$

         # Trigger to disallow updating bookings with past arrival and departure dates to bring them in the future
         # and to disallow the new dates to be in the past
         DELIMITER $$
         DROP TRIGGER IF EXISTS DATES_UPDATE_TRIGGER_PAST
         $$
         CREATE TRIGGER DATES_UPDATE_TRIGGER_PAST
         BEFORE UPDATE ON BOOKINGS
         FOR EACH ROW
         BEGIN
            IF (OLD.DEPARTURE_DATE < CURDATE() OR OLD.ARRIVAL_DATE < CURDATE()) THEN
                SIGNAL SQLSTATE '45000'
                     SET MESSAGE_TEXT = 'Cannot update a booking from the past';
            ELSEIF (NEW.DEPARTURE_DATE < CURDATE() OR NEW.ARRIVAL_DATE < CURDATE()) THEN
                    SIGNAL SQLSTATE '45000'
                        SET MESSAGE_TEXT = 'Cannot update a booking so that the arrival and departure dates are in the past';
            END IF;
         END;
         $$

         #A trigger to make sure that the ARRIVAL_DATE and DEPARTURE_DATE are both in the future was not created
         #in case the DB gets accidentally wiped out and past data needs to be restored with a backup.