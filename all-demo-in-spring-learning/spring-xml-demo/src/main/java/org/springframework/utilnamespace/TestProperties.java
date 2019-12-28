package org.springframework.utilnamespace;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
public class TestProperties {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:util-namespace-test-properties.xml"},false);
        context.refresh();

        List<BeanDefinition> list =
                context.getBeanFactory().getBeanDefinitionList();
        log.info("bean definition list:{}", JSON.toJSONString(list,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.PrettyFormat));

        Object o = context.getBean("confTest");
        System.out.println(o);
    }
}
