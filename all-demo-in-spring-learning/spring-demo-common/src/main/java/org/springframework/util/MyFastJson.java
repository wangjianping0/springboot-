package org.springframework.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;

import java.util.List;

@Slf4j
public class MyFastJson {

    public static void printJsonStringForBeanDefinitionList(List<BeanDefinition> list) {
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(MutablePropertyValues.class);
        filter.getExcludes().add("propertyValues");
        log.info("bean definition list:{}", JSON.toJSONString(list,
                filter,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.PrettyFormat));

    }
}
