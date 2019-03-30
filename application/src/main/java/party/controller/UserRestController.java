package party.controller;

import base.controller.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import party.service.UserService;

/**
 * @author csieflyman
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/users", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ResponseBody
public class UserRestController extends AbstractController {

    @Autowired
    private UserService userService;

}
