package org.springframework.simple;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Json2BeanDefinitionUtil {
    public static void main(String[] args) throws IOException {
        ClassLoader classLoader = Json2BeanDefinitionUtil.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("beanDefinition.json");
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(reader);

        String line = null;
        StringBuilder builder = new StringBuilder("");
        ArrayList<String> list = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            builder.append(line);
        }

        System.out.println(builder.toString());


        List<GenericBeanDefinition> array = JSONObject.parseArray(builder.toString(), GenericBeanDefinition.class);
        System.out.println(array);

    }
}
