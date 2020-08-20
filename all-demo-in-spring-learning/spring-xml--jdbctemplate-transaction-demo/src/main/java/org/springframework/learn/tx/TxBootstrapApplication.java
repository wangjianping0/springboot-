package org.springframework.learn.tx;


import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TxBootstrapApplication {



	public static void main(String[] args) throws Exception {
// 获得Spring容器，并操作
		String xmlPath = "application-context.xml";
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				xmlPath);
		AccountService accountService = (AccountService) applicationContext
				.getBean("accountService");
		accountService.transfer("zhangsan", "lisi", 100);

	}
}
