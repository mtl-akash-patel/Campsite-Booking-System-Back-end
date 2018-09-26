import com.akash.campsite.service.CampsiteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.format.DateTimeParseException;


/**
 * Created by Kash on 9/25/2018.
 *
 * Simple test class to show how I would test the service. I would essentially trigger every exception that can be thrown
 * in all possible cases and I would also ideally have a happy path test case.
 *
 * Ideally, I would also have integration tests.
 *
 * Ideally the service should be autowired instead of instantiated
 */

@RunWith(SpringRunner.class)
public class CampsiteServiceTest {

    private CampsiteService campsiteService;

    private String firstName, lastName, email, arrivalDateString, departureDateString;

    @Before
    public void setup() {
        campsiteService = new CampsiteService();
        arrivalDateString = "2018-10-10";
        departureDateString = "2018-10-11";
        firstName = "Jon";
        lastName = "Doe";
        email = "jondoe@gmail.com";
    }

    // The test should also be done for lastname and email.
    @Test (expected=IllegalArgumentException.class)
    public void attemptToCreateBookingTestNullFirstName() throws Exception{
        firstName = null;
        campsiteService.attemptToCreateBooking(firstName, lastName, email, arrivalDateString, departureDateString);
    }

    // The test should also be done for lastname and email.
    @Test (expected=IllegalArgumentException.class)
    public void attemptToCreateBookingTestEmptyFirstName() throws Exception{
        firstName = "";
        campsiteService.attemptToCreateBooking(firstName, lastName, email, arrivalDateString, departureDateString);
    }

    // The test should also be done for departureDateString
    @Test (expected=IllegalArgumentException.class)
    public void attemptToCreateBookingTestNullArrivalDate() throws Exception{
        arrivalDateString = null;
        campsiteService.attemptToCreateBooking(firstName, lastName, email, arrivalDateString, departureDateString);
    }

    // Same test as above but for departureSteString
    @Test (expected=IllegalArgumentException.class)
    public void attemptToCreateBookingTestNullDepartureDate() throws Exception{
        departureDateString = null;
        campsiteService.attemptToCreateBooking(firstName, lastName, email, arrivalDateString, departureDateString);
    }

    // The test should also be done for departureDateString
    @Test (expected=DateTimeParseException.class)
    public void attemptToCreateBookingTestInvalidArrivalDate() throws Exception{
        arrivalDateString = "2018-090=09";
        campsiteService.attemptToCreateBooking(firstName, lastName, email, arrivalDateString, departureDateString);
    }
}
