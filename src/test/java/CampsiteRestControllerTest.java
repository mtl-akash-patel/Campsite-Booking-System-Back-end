import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Created by Kash on 9/25/2018.
 */


public class CampsiteRestControllerTest {

    private final String uriBase =  "http://localhost:8080/campsite/";
    private final String availabilityUri = "availability";

    @Test
    public void testGetAvailability() {
        // Given
        HttpUriRequest request = new HttpGet(uriBase + availabilityUri);
        try {
            // When
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            //Then
            assertEquals(HttpStatus.OK, response.getStatusLine());
        }
        catch (IOException e) {
            fail();
        }
    }
}
