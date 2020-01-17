package org.springframework.contextnamespace.componentscan;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Slf4j
@Getter
@Setter
@Controller
public class PersonTestController {

//    @Autowired
    @Resource
    private PersonService personService;
}
