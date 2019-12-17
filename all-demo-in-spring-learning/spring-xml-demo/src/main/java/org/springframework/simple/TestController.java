package org.springframework.simple;

public class TestController {

    TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }
}
