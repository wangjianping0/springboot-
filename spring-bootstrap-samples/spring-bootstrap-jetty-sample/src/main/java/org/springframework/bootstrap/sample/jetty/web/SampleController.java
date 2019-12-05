package org.springframework.bootstrap.sample.jetty.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.bootstrap.sample.jetty.service.HelloWorldService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SampleController {

	@Autowired
	private HelloWorldService helloWorldService;

	@RequestMapping("/")
	@ResponseBody
	public String helloWorld() {
		return helloWorldService.getHelloMessage();
	}
}
