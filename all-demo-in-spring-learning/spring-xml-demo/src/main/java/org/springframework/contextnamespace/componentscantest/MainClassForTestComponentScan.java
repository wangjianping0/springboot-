package org.springframework.contextnamespace.componentscantest;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.MyFastJson;

import java.util.List;
import java.util.Map;

@Slf4j
public class MainClassForTestComponentScan {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{"classpath:context-namespace-test-component-scan.xml"},false);
        context.refresh();


        List<BeanDefinition> list =
                context.getBeanFactory().getBeanDefinitionList();
        MyFastJson.printJsonStringForBeanDefinitionList(list);

        Object bean = context.getBean(PersonTestController.class);
        System.out.println("PersonController bean:" + bean);

    }
}
