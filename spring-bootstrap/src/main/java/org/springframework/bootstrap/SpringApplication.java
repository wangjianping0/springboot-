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

import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.bootstrap.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * Classes that can be used to bootstrap and launch a Spring application from a Java main
 * method. By default class will perform the following steps to bootstrap your
 * application:
 * 用来从一个java main方法启动spring应用的类。默认情况下，本类会进行以下步骤来启动应用：
 * <ul>
 *     创建一个合适的上下文对象，注解驱动的上下文，或者注解驱动的内置的web 应用上下文
 * <li>Create an appropriate {@link ApplicationContext} instance (
 * {@link AnnotationConfigApplicationContext} or
 * {@link AnnotationConfigEmbeddedWebApplicationContext} depending on your classpath)</li>
 *
 *     注册一个CommandLinePropertySource 暴露命令行参数给应用
 * <li>Register a {@link CommandLinePropertySource} to expose command line arguments as
 * Spring properties</li>
 *     加载上下文，加载所有的单例bean
 * <li>Refresh the application context, loading all singleton beans</li>
 *     触发CommandLineRunner
 * <li>Trigger any {@link CommandLineRunner} beans</li>
 * </ul>
 *   大部分情况下，静态的run(Object, String[]) 可以直接在main中调用，启动应用
 *   注意，run方法要指定一个source对象，应该指的是主配置类
 * In most circumstances the static {@link #run(Object, String[])} method can be called
 * directly from your {@literal main} method to bootstrap your application:
 * 
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAutoConfiguration
 * public class MyApplication  {
 * 
 * // ... Bean definitions
 * 
 * public static void main(String[] args) throws Exception {
 *   SpringApplication.run(MyApplication.class, args);
 * }
 * </pre>
 * 
 * <p>
 *     如果要定制的话，可以先new出来SpringApplication，然后再定制
 * For more advanced configuration a {@link SpringApplication} instance can be created and
 * customized before being run:
 * 
 * <pre class="code">
 * public static void main(String[] args) throws Exception {
 *   SpringApplication app = new SpringApplication(MyApplication.class);
 *   // ... customize app settings here
 *   app.run(args)
 * }
 * </pre>
 *  SpringApplication可以从很多不同的源，读取bean。推荐使用一个单一的Configuration来启动应用
 * {@link SpringApplication}s can read beans from a variety of different sources. It is
 * generally recommended that a single {@code @Configuration} class is used to bootstrap
 * your application, however, any of the following sources can also be used:
 * 
 * <p>
 * <ul>
 * <li>{@link Class} - A Java class to be loaded by {@link AnnotatedBeanDefinitionReader}</li>
 * 
 * <li>{@link Resource} - A XML resource to be loaded by {@link XmlBeanDefinitionReader}</li>
 * 
 * <li>{@link Package} - A Java package to be scanned by
 * {@link ClassPathBeanDefinitionScanner}</li>
 * 
 * <li>{@link CharSequence} - A class name, resource handle or package name to loaded as
 * appropriate. If the {@link CharSequence} cannot be resolved to class and does not
 * resolve to a {@link Resource} that exists it will be considered a {@link Package}.</li>
 * </ul>
 * 
 * @author Phillip Webb
 * @author Dave Syer
 * @see #run(Object, String[])
 * @see #run(Object[], String[])
 * @see #SpringApplication(Object...)
 */
public class SpringApplication {

	private static final String DEFAULT_CONTEXT_CLASS = "org.springframework.context."
			+ "annotation.AnnotationConfigApplicationContext";

	private static final String DEFAULT_WEB_CONTEXT_CLASS = "org.springframework.bootstrap."
			+ "context.embedded.AnnotationConfigEmbeddedWebApplicationContext";

	private static final String[] WEB_ENVIRONMENT_CLASSES = { "javax.servlet.Servlet",
			"org.springframework.web.context.ConfigurableWebApplicationContext" };

	private Object[] sources;

	private boolean showBanner = true;

	private boolean addCommandLineProperties = true;

	private ResourceLoader resourceLoader;

	private BeanNameGenerator beanNameGenerator;

	private ConfigurableEnvironment environment;

	private ApplicationContext applicationContext;

	private Class<? extends ApplicationContext> applicationContextClass;

	private boolean webEnvironment;
	/**
	 * 初始化器列表
	 */
	private List<ApplicationContextInitializer<?>> initializers;

	/**
	 * Crate a new {@link SpringApplication} instance. The application context will load
	 * beans from the specified sources (see {@link SpringApplication class-level}
	 * documentation for details. The instance can be customized before calling
	 * {@link #run(String...)}.
	 * @param sources the bean sources
	 * @see #run(Object, String[])
	 * @see #SpringApplication(ResourceLoader, Object...)
	 */
	public SpringApplication(Object... sources) {
		Assert.notEmpty(sources, "Sources must not be empty");
		this.sources = sources;
		initialize();
	}

	/**
	 * Crate a new {@link SpringApplication} instance. The application context will load
	 * beans from the specified sources (see {@link SpringApplication class-level}
	 * documentation for details. The instance can be customized before calling
	 * {@link #run(String...)}.
	 * @param resourceLoader the resource loader to use
	 * @param sources the bean sources
	 * @see #run(Object, String[])
	 * @see #SpringApplication(ResourceLoader, Object...)
	 */
	public SpringApplication(ResourceLoader resourceLoader, Object... sources) {
		Assert.notEmpty(sources, "Sources must not be empty");
		this.resourceLoader = resourceLoader;
		this.sources = sources;
		initialize();
	}

	protected void initialize() {
		this.webEnvironment = deduceWebEnvironment();
		this.initializers = new ArrayList<ApplicationContextInitializer<?>>();
		/**
		 * 获取初始化器，从 spring.factories 中获取
		 */
		@SuppressWarnings("rawtypes")
		Collection<ApplicationContextInitializer> factories = loadFactories(ApplicationContextInitializer.class,
						SpringApplication.class.getClassLoader());
		for (ApplicationContextInitializer<?> initializer : factories) {
			this.initializers.add(initializer);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T instantiateFactory(String instanceClassName, Class<T> factoryClass, ClassLoader classLoader) {
		try {
			Class<?> instanceClass = ClassUtils.forName(instanceClassName, classLoader);
			if (!factoryClass.isAssignableFrom(instanceClass)) {
				throw new IllegalArgumentException(
						"Class [" + instanceClassName + "] is not assignable to [" + factoryClass.getName() + "]");
			}
			return (T) instanceClass.newInstance();
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException("Cannot instantiate factory class: " + factoryClass.getName(), ex);
		}
	}

	/** The location to look for the factories. Can be present in multiple JAR files. */
	private static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";
	private static final Log logger = LogFactory.getLog(SpringFactoriesLoader.class);

	public static <T> List<T> loadFactories(Class<T> factoryClass, ClassLoader classLoader) {
		Assert.notNull(factoryClass, "'factoryClass' must not be null");
		if (classLoader == null) {
			classLoader = SpringFactoriesLoader.class.getClassLoader();
		}
		List<String> factoryNames = loadFactoryNames(factoryClass, classLoader);
		if (logger.isTraceEnabled()) {
			logger.trace("Loaded [" + factoryClass.getName() + "] names: " + factoryNames);
		}
		List<T> result = new ArrayList<T>(factoryNames.size());
		for (String factoryName : factoryNames) {
			result.add(instantiateFactory(factoryName, factoryClass, classLoader));
		}
		OrderComparator.sort(result);
		return result;
	}


	public static List<String> loadFactoryNames(Class<?> factoryClass, ClassLoader classLoader) {
		String factoryClassName = factoryClass.getName();
		try {
			List<String> result = new ArrayList<String>();
			Enumeration<URL> urls = classLoader.getResources(FACTORIES_RESOURCE_LOCATION);
			ArrayList<URL> list = Collections.list(urls);
			for (URL url : list) {
				Properties properties = PropertiesLoaderUtils.loadProperties(new UrlResource(url));
				String factoryClassNames = properties.getProperty(factoryClassName);
				result.addAll(Arrays.asList(StringUtils.commaDelimitedListToStringArray(factoryClassNames)));
			}
			return result;
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load [" + factoryClass.getName() +
					"] factories from location [" + FACTORIES_RESOURCE_LOCATION + "]", ex);
		}
	}

	private boolean deduceWebEnvironment() {
		for (String className : WEB_ENVIRONMENT_CLASSES) {
			if (!ClassUtils.isPresent(className, null)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Run the Spring application, creating and refreshing a new
	 * {@link ApplicationContext}.
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return a running {@link ApplicationContext}
	 */
	public ApplicationContext run(String... args) {
		if (this.showBanner) {
			printBanner();
		}
		ApplicationContext context = createApplicationContext();
		/**
		 * 后置处理上下文，提供给子类的扩展钩子
		 */
		postProcessApplicationContext(context);
		addPropertySources(context, args);
		/**
		 * 应用初始化器
		 */
		if (context instanceof ConfigurableApplicationContext) {
			applyInitializers((ConfigurableApplicationContext) context);
		}
		load(context, this.sources);
		refresh(context);
		runCommandLineRunners(context, args);
		return context;
	}

	/**
	 * Print a simple banner message to the console. Subclasses can override this method
	 * to provide additional or alternative banners.
	 * @see #setShowBanner(boolean)
	 */
	protected void printBanner() {
		Banner.write(System.out);
	}

	/**
	 * Apply any {@link ApplicationContextInitializer}s to the context before it is
	 * refreshed.
	 * @param context the configured ApplicationContext (not refreshed yet)
	 * @see ConfigurableApplicationContext#refresh()
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void applyInitializers(ConfigurableApplicationContext context) {
		for (ApplicationContextInitializer initializer : this.initializers) {
			Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(
					initializer.getClass(), ApplicationContextInitializer.class);
			Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
			initializer.initialize(context);
		}
	}

	/**
	 * Strategy method used to create the {@link ApplicationContext}. By default this
	 * method will respect any explicitly set application context or application context
	 * class before falling back to a suitable default.
	 * @return the application context (not yet refreshed)
	 * @see #setApplicationContext(ApplicationContext)
	 * @see #setApplicationContextClass(Class)
	 */
	protected ApplicationContext createApplicationContext() {
		if (this.applicationContext != null) {
			return this.applicationContext;
		}

		Class<?> contextClass = this.applicationContextClass;
		if (contextClass == null) {
			try {
				contextClass = Class
						.forName(this.webEnvironment ? DEFAULT_WEB_CONTEXT_CLASS
								: DEFAULT_CONTEXT_CLASS);
			} catch (ClassNotFoundException ex) {
				throw new IllegalStateException(
						"Unable create a default ApplicationContext, "
								+ "please specify an ApplicationContextClass", ex);
			}
		}

		return (ApplicationContext) BeanUtils.instantiate(contextClass);
	}

	/**
	 * Apply any relevant post processing the {@link ApplicationContext}. Subclasses can
	 * apply additional processing as required.
	 * @param context the application context
	 */
	protected void postProcessApplicationContext(ApplicationContext context) {
		if (this.webEnvironment) {
			if (context instanceof ConfigurableWebApplicationContext) {
				ConfigurableWebApplicationContext configurableContext = (ConfigurableWebApplicationContext) context;
				if (this.beanNameGenerator != null) {
					configurableContext.getBeanFactory().registerSingleton(
							AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR,
							this.beanNameGenerator);
				}
			}
		}

		if (context instanceof AbstractApplicationContext) {
			((AbstractApplicationContext) context).setEnvironment(this.environment);
		}

		if (context instanceof GenericApplicationContext) {
			((GenericApplicationContext) context).setResourceLoader(this.resourceLoader);
		}
	}

	/**
	 * Add any {@link PropertySource}s to the application context environment.
	 * @param context the application context
	 * @param args run arguments
	 */
	protected void addPropertySources(ApplicationContext context, String[] args) {
		Environment environment = context.getEnvironment();
		if (environment instanceof ConfigurableEnvironment) {
			ConfigurableEnvironment configurable = (ConfigurableEnvironment) environment;
			if (this.addCommandLineProperties) {
				CommandLinePropertySource<?> propertySource = new SimpleCommandLinePropertySource(
						args);
				configurable.getPropertySources().addFirst(propertySource);
			}
		}
	}

	/**
	 * Load beans into the application context.
	 * @param context the context to load beans into
	 */
	protected void load(ApplicationContext context, Object[] sources) {
		Assert.isInstanceOf(BeanDefinitionRegistry.class, context);
		/**
		 * bean定义加载器
		 */
		BeanDefinitionLoader loader = createBeanDefinitionLoader(
				(BeanDefinitionRegistry) context, sources);
		if (this.beanNameGenerator != null) {
			loader.setBeanNameGenerator(this.beanNameGenerator);
		}
		if (this.resourceLoader != null) {
			loader.setResourceLoader(this.resourceLoader);
		}
		if (this.environment != null) {
			loader.setEnvironment(this.environment);
		}
		/**
		 * 加载beanDefinition
		 */
		loader.load();
	}

	/**
	 * Factory method used to create the {@link BeanDefinitionLoader}.
	 */
	protected BeanDefinitionLoader createBeanDefinitionLoader(
			BeanDefinitionRegistry registry, Object[] sources) {
		return new BeanDefinitionLoader(registry, sources);
	}

	private void runCommandLineRunners(ApplicationContext context, String... args) {
		List<CommandLineRunner> runners = new ArrayList<CommandLineRunner>(context
				.getBeansOfType(CommandLineRunner.class).values());
		AnnotationAwareOrderComparator.sort(runners);
		for (CommandLineRunner runner : runners) {
			runner.run(args);
		}
	}

	/**
	 * Refresh the underlying {@link ApplicationContext}.
	 * @param applicationContext the application context to refresh
	 */
	protected void refresh(ApplicationContext applicationContext) {
		Assert.isInstanceOf(AbstractApplicationContext.class, applicationContext);
		((AbstractApplicationContext) applicationContext).refresh();
	}

	/**
	 * Sets if this application is running within a web environment. If not specified will
	 * attempt to deduce the environment based on the classpath.
	 */
	public void setWebEnvironment(boolean webEnvironment) {
		this.webEnvironment = webEnvironment;
	}

	/**
	 * Sets if the Spring banner should be displayed when the application runs. Defaults
	 * to {@code true}.
	 * @see #printBanner()
	 */
	public void setShowBanner(boolean showBanner) {
		this.showBanner = showBanner;
	}

	/**
	 * Sets if a {@link CommandLinePropertySource} should be added to the application
	 * context in order to expose arguments. Defaults to {@code true}.
	 */
	public void setAddCommandLineProperties(boolean addCommandLineProperties) {
		this.addCommandLineProperties = addCommandLineProperties;
	}

	/**
	 * Sets the bean name generator that should be used when generating bean names.
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
	}

	/**
	 * Sets the underlying environment that should be used when loading.
	 */
	public void setEnvironment(ConfigurableEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * Sets the {@link ResourceLoader} that should be used when loading resources.
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Sets the type of Spring {@link ApplicationContext} that will be created. If not
	 * specified defaults to {@link AnnotationConfigEmbeddedWebApplicationContext} for web
	 * based applications or {@link AnnotationConfigApplicationContext} for non web based
	 * applications.
	 * @param applicationContextClass the context class to set
	 * @see #setApplicationContext(ApplicationContext)
	 */
	public void setApplicationContextClass(
			Class<? extends ApplicationContext> applicationContextClass) {
		this.applicationContextClass = applicationContextClass;
	}

	/**
	 * Sets a Spring {@link ApplicationContext} that will be used for the application. If
	 * not specified an {@link AnnotationConfigEmbeddedWebApplicationContext} will be
	 * created for web based applications or an {@link AnnotationConfigApplicationContext}
	 * for non web based applications.
	 * @param applicationContext the spring application context.
	 * @see #setApplicationContextClass(Class)
	 */
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * @param initializers the initializers to set
	 */
	public void setInitializers(
			Collection<? extends ApplicationContextInitializer<?>> initializers) {
		this.initializers = new ArrayList<ApplicationContextInitializer<?>>(initializers);
	}

	/**
	 * @param initializers
	 */
	public void addInitializers(ApplicationContextInitializer<?>... initializers) {
		this.initializers.addAll(Arrays.asList(initializers));
	}

	/**
	 * @return the initializers
	 */
	public List<ApplicationContextInitializer<?>> getInitializers() {
		return this.initializers;
	}

	/**
	 * Static helper that can be used to run a {@link SpringApplication} from the
	 * specified source using default settings.
	 * @param source the source to load
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return the running {@link ApplicationContext}
	 */
	public static ApplicationContext run(Object source, String... args) {
		return run(new Object[] { source }, args);
	}

	/**
	 * Static helper that can be used to run a {@link SpringApplication} from the
	 * specified sources using default settings.
	 * @param sources the sources to load
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return the running {@link ApplicationContext}
	 */
	public static ApplicationContext run(Object[] sources, String[] args) {
		return new SpringApplication(sources).run(args);
	}

	/**
	 * Static helper that can be used to run a {@link SpringApplication} from the
	 * specified {@code @Component} sources. Any class not annotated or meta-annotated
	 * with {@code @Component} will be ignored.
	 * @param classes the source classes to consider loading
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return the running {@link ApplicationContext}
	 */
	public static ApplicationContext runComponents(Class<?>[] classes, String... args) {
		List<Class<?>> componentClasses = new ArrayList<Class<?>>();
		for (Class<?> candidate : classes) {
			StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(
					candidate);
			if (metadata.isAnnotated(Component.class.getName())) {
				componentClasses.add(candidate);
			}
		}
		return new SpringApplication(componentClasses.toArray()).run(args);
	}
}
