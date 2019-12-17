package org.springframework.simple;


import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SimpleBootstrapApplication  {



	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:application-context.xml");
		TestController testController = (TestController) context.getBean("testController");
		System.out.println(testController);
	}
}
