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
         DROP TRIGGER IF EXISTS DATES_UPDATE_TRIGGER
         $$
         CREATE TRIGGER DATES_UPDATE_TRIGGER
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

         #A trigger to make sure that the ARRIVAL_DATE and DEPARTURE_DATE are both in the future were not created
         #in case the DB gets accidentally wiped out and passed data needs to be restored with a backup.