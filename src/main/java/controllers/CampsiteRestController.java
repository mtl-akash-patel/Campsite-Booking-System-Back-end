package controllers;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by Kash on 9/22/2018.
 */

@RestController
public class CampsiteRestController {

    @RequestMapping(value = "/greeting", method = GET)
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hello " + name;
    }

    @RequestMapping(value = "/create_user", method = POST)
    public String createUser(@RequestBody String firstName, String lastName, String email) {
        return "Generated user";
    }
}
