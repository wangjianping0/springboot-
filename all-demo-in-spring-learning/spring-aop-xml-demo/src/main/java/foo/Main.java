package foo;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.MyFastJson;

import java.util.List;

public final class Main {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "context-namespace-test-aop.xml");
        List<BeanDefinition> list =
                ctx.getBeanFactory().getBeanDefinitionList();
        MyFastJson.printJsonStringForBeanDefinitionList(list);
        Perform performer = (Perform) ctx.getBean(Perform.class);
        performer.sing();
    }
}
