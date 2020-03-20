package org.springframework.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BootStrap {

    public static void main(String[] args) {
//        testAssignable();
//        testAspectj();
        testCustom();
    }


    public static void testAnnotationFilter() {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:component-scan-annotation-filter.xml");
        String[] beanDefinitionNames = context.getBeanDefinitionNames();

        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);
        }

    }

    static void testAssignable() {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:component-scan-assignable-filter.xml");
        String[] beanDefinitionNames = context.getBeanDefinitionNames();

        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);
        }
    }

    static void testAspectj() {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext(
                        "classpath:component-scan-aspectj-filter.xml");
        String[] beanDefinitionNames = context.getBeanDefinitionNames();

        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);
        }
    }


    static void testCustom() {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext(
                        "classpath:component-scan-custom-filter.xml");
        String[] beanDefinitionNames = context.getBeanDefinitionNames();

        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);
        }
    }
}
