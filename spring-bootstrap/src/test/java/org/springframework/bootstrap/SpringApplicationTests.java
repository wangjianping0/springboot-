/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.bootstrap;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.bootstrap.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.bootstrap.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link SpringApplication}.
 * 
 * @author Phillip Webb
 */
public class SpringApplicationTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private ApplicationContext context;

	@After
	public void close() {
		if (this.context instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) this.context).close();
		}
	}

	@Test
	public void sourcesMustNotBeNull() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Sources must not be empty");
		new SpringApplication((Object[]) null);
	}

	@Test
	public void sourcesMustNotBeEmpty() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Sources must not be empty");
		new SpringApplication();
	}

	@Test
	public void disableBanner() throws Exception {
		SpringApplication application = spy(new SpringApplication(ExampleConfig.class));
		application.setWebEnvironment(false);
		application.setShowBanner(false);
		application.run();
		verify(application, never()).printBanner();
	}

	@Test
	public void customBanner() throws Exception {
		SpringApplication application = spy(new SpringApplication(ExampleConfig.class));
		application.setWebEnvironment(false);
		application.run();
		verify(application).printBanner();
	}

	@Test
	public void specificApplicationContext() throws Exception {
		SpringApplication application = new SpringApplication(ExampleConfig.class);
		ApplicationContext applicationContext = new StaticApplicationContext();
		application.setApplicationContext(applicationContext);
		this.context = application.run();
		assertThat(this.context, sameInstance(applicationContext));
	}

	@Test
	public void specificApplicationContextClass() throws Exception {
		SpringApplication application = new SpringApplication(ExampleConfig.class);
		application.setApplicationContextClass(StaticApplicationContext.class);
		this.context = application.run();
		assertThat(this.context, instanceOf(StaticApplicationContext.class));
	}

	@Test
	public void defaultApplicationContext() throws Exception {
		SpringApplication application = new SpringApplication(ExampleConfig.class);
		application.setWebEnvironment(false);
		this.context = application.run();
		assertThat(this.context, instanceOf(AnnotationConfigApplicationContext.class));
	}

	@Test
	public void defaultApplicationContextForWeb() throws Exception {
		SpringApplication application = new SpringApplication(ExampleWebConfig.class);
		application.setWebEnvironment(true);
		this.context = application.run();
		assertThat(this.context,
				instanceOf(AnnotationConfigEmbeddedWebApplicationContext.class));
	}

	@Test
	public void customEnvironment() throws Exception {
		TestSpringApplication application = new TestSpringApplication(ExampleConfig.class);
		application.setWebEnvironment(false);
		StaticApplicationContext applicationContext = spy(new StaticApplicationContext());
		ConfigurableEnvironment environment = new StandardEnvironment();
		application.setApplicationContext(applicationContext);
		application.setEnvironment(environment);
		application.run();
		verify(applicationContext).setEnvironment(environment);
		verify(application.getLoader()).setEnvironment(environment);
	}

	@Test
	public void customResourceLoader() throws Exception {
		TestSpringApplication application = new TestSpringApplication(ExampleConfig.class);
		application.setWebEnvironment(false);
		StaticApplicationContext applicationContext = spy(new StaticApplicationContext());
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		application.setApplicationContext(applicationContext);
		application.setResourceLoader(resourceLoader);
		application.run();
		verify(applicationContext).setResourceLoader(resourceLoader);
		verify(application.getLoader()).setResourceLoader(resourceLoader);
	}

	public static void main(String[] args) throws Exception {
		new SpringApplicationTests().customResourceLoaderFromConstructor();
	}
	@Test
	public void customResourceLoaderFromConstructor() throws Exception {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		TestSpringApplication application = new TestSpringApplication(resourceLoader,
				ExampleWebConfig.class);
		StaticApplicationContext applicationContext = spy(new StaticApplicationContext());
		application.setApplicationContext(applicationContext);
		application.run();
		verify(applicationContext).setResourceLoader(resourceLoader);
		verify(application.getLoader()).setResourceLoader(resourceLoader);
		applicationContext.close();
	}

	@Test
	public void customBeanNameGenerator() throws Exception {
		TestSpringApplication application = new TestSpringApplication(
				ExampleWebConfig.class);
		StaticWebApplicationContext applicationContext = spy(new StaticWebApplicationContext());
		applicationContext.setServletContext(new MockServletContext());
		BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
		application.setApplicationContext(applicationContext);
		application.setBeanNameGenerator(beanNameGenerator);
		this.context = application.run();
		verify(application.getLoader()).setBeanNameGenerator(beanNameGenerator);
		assertThat(
				this.context
						.getBean(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR),
				sameInstance((Object) beanNameGenerator));
	}

	@Test
	public void commandLinePropertySource() throws Exception {
		SpringApplication application = new SpringApplication(ExampleConfig.class);
		application.setWebEnvironment(false);
		ConfigurableEnvironment environment = new StandardEnvironment();
		application.setEnvironment(environment);
		application.run();
		assertThat(hasPropertySource(environment, CommandLinePropertySource.class),
				equalTo(true));
	}

	@Test
	public void disableCommandLinePropertySource() throws Exception {
		SpringApplication application = new SpringApplication(ExampleConfig.class);
		application.setWebEnvironment(false);
		application.setAddCommandLineProperties(false);
		ConfigurableEnvironment environment = new StandardEnvironment();
		application.setEnvironment(environment);
		application.run();
		assertThat(hasPropertySource(environment, CommandLinePropertySource.class),
				equalTo(false));
	}

	@Test
	public void runCommandLineRunners() throws Exception {
		SpringApplication application = new SpringApplication(CommandLineRunConfig.class);
		application.setWebEnvironment(false);
		this.context = application.run("arg");
		assertTrue(this.context.getBean("runnerA", TestCommandLineRunner.class).hasRun());
		assertTrue(this.context.getBean("runnerB", TestCommandLineRunner.class).hasRun());
	}

	@Test
	public void loadSources() throws Exception {
		Object[] sources = { ExampleConfig.class, "a", TestCommandLineRunner.class };
		TestSpringApplication application = new TestSpringApplication(sources);
		application.setWebEnvironment(false);
		application.setUseMockLoader(true);
		application.run();
		assertThat(application.getSources(), equalTo(sources));
	}

	@Test
	public void run() throws Exception {
		this.context = SpringApplication.run(ExampleWebConfig.class);
		assertNotNull(this.context);
	}

	@Test
	public void runComponents() throws Exception {
		this.context = SpringApplication.runComponents(new Class<?>[] {
				ExampleWebConfig.class, Object.class });
		assertNotNull(this.context);
	}

	private boolean hasPropertySource(ConfigurableEnvironment environment,
			Class<?> propertySourceClass) {
		for (PropertySource<?> source : environment.getPropertySources()) {
			if (propertySourceClass.isInstance(source)) {
				return true;
			}
		}
		return false;
	}

	// FIXME test initializers

	// FIXME test config files?

	private static class TestSpringApplication extends SpringApplication {

		private BeanDefinitionLoader loader;

		private boolean useMockLoader;

		private Object[] sources;

		public TestSpringApplication(Object... sources) {
			super(sources);
		}

		public TestSpringApplication(ResourceLoader resourceLoader, Object... sources) {
			super(resourceLoader, sources);
		}

		public void setUseMockLoader(boolean useMockLoader) {
			this.useMockLoader = useMockLoader;
		}

		@Override
		protected BeanDefinitionLoader createBeanDefinitionLoader(
				BeanDefinitionRegistry registry, Object[] sources) {
			this.sources = sources;
			if (this.useMockLoader) {
				this.loader = mock(BeanDefinitionLoader.class);
			} else {
				this.loader = spy(super.createBeanDefinitionLoader(registry, sources));
			}
			return this.loader;
		}

		public BeanDefinitionLoader getLoader() {
			return this.loader;
		}

		public Object[] getSources() {
			return this.sources;
		}
	}

	@Configuration
	static class ExampleConfig {

	}

	@Configuration
	static class ExampleWebConfig {

		@Bean
		public JettyEmbeddedServletContainerFactory container() {
			return new JettyEmbeddedServletContainerFactory();
		}

	}

	@Configuration
	static class CommandLineRunConfig {

		@Bean
		public TestCommandLineRunner runnerB() {
			return new TestCommandLineRunner(Ordered.LOWEST_PRECEDENCE, "runnerA");
		}

		@Bean
		public TestCommandLineRunner runnerA() {
			return new TestCommandLineRunner(Ordered.HIGHEST_PRECEDENCE);
		}
	}

	static class TestCommandLineRunner implements CommandLineRunner,
			ApplicationContextAware, Ordered {

		private String[] expectedBefore;

		private ApplicationContext applicationContext;

		private String[] args;

		private int order;

		public TestCommandLineRunner(int order, String... expectedBefore) {
			this.expectedBefore = expectedBefore;
			this.order = order;
		}

		@Override
		public void setApplicationContext(ApplicationContext applicationContext)
				throws BeansException {
			this.applicationContext = applicationContext;
		}

		@Override
		public int getOrder() {
			return this.order;
		}

		@Override
		public void run(String... args) {
			this.args = args;
			for (String name : this.expectedBefore) {
				TestCommandLineRunner bean = this.applicationContext.getBean(name,
						TestCommandLineRunner.class);
				assertTrue(bean.hasRun());
			}
		}

		public boolean hasRun() {
			return this.args != null;
		}

	}
}
