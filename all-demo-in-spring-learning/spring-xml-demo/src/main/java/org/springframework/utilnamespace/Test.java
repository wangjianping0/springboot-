package org.springframework.utilnamespace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * desc:
 *
 * @author : caokunliang
 * creat_date: 2019/12/25 0025
 * creat_time: 15:50
 **/
@Slf4j
public class Test {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:util-namespace-test.xml"},false);
        context.setAllowBeanDefinitionOverriding(true);
        context.refresh();

        List<BeanDefinition> list =
                context.getBeanFactory().getBeanDefinitionList();
        log.info("bean definition list:{}",list);
//        Object testService = context.getBean("&chin.age");
//        System.out.println("factory:" + testService);

        Object o = context.getBean("confTest");
        System.out.println(o);
    }
}
