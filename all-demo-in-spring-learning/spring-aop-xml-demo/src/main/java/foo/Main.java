package foo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class Main {

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "context-namespace-test-aop.xml");
        Perform performer = (Perform) ctx.getBean(Perform.class);
        performer.sing();
    }
}
