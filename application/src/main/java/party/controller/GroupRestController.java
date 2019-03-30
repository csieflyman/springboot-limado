package party.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import party.service.GroupService;

/**
 * @author csieflyman
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/groups", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class GroupRestController {

    @Autowired
    private GroupService groupService;

}
