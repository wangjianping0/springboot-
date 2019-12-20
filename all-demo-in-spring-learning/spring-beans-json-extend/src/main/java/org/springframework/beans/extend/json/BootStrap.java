package org.springframework.beans.extend.json;

import org.springframework.beans.extend.json.applicationcontext.ClassPathJsonApplicationContext;
import org.springframework.simple.byconstructor.TestControllerByConstructor;

public class BootStrap {
    public static void main(String[] args) {
        ClassPathJsonApplicationContext context = new ClassPathJsonApplicationContext("beanDefinition.json");
        TestControllerByConstructor bean = context.getBean(TestControllerByConstructor.class);
        System.out.println(bean);
    }
}
