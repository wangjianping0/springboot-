package org.springframework.bootstrap.sample.simple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.bootstrap.CommandLineRunner;
import org.springframework.bootstrap.SpringApplication;
import org.springframework.bootstrap.context.annotation.EnableAutoConfiguration;
import org.springframework.bootstrap.sample.simple.service.MessageService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class ProfileBootstrapApplication implements CommandLineRunner {

	// Simple example shows how a command line spring application can execute an
	// injected bean service. Also demonstrates how you can use @Value to inject
	// command line args ('--name=whatever') or application properties

	@Autowired
	private MessageService helloWorldService;

	@Override
	public void run(String... args) {
		System.out.println(this.helloWorldService.getMessage());
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ProfileBootstrapApplication.class, args);
	}
}
