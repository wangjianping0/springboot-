package org.springframework.contextnamespace.componentscantest;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Slf4j
@Data
@Controller
public class PersonTestController {

    @Autowired
    private PersonService personService;
}
