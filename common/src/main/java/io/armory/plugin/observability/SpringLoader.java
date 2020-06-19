package io.armory.plugin.observability;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.io.DefaultResourceLoader;

@Slf4j
public class SpringLoader implements ApplicationContextAware {

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    if (!(applicationContext instanceof AnnotationConfigServletWebServerApplicationContext)) {
      log.error("Application is not generic, unable to install discovered beans");
    }
    AnnotationConfigServletWebServerApplicationContext aCtx =
        (AnnotationConfigServletWebServerApplicationContext) applicationContext;

    ClassLoader pluginClassLoader = getClass().getClassLoader();
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.setParent(applicationContext);
    ctx.setClassLoader(pluginClassLoader);

    // Process configuration classes
    ConfigurationClassPostProcessor configPostProcessor = new ConfigurationClassPostProcessor();
    configPostProcessor.setBeanClassLoader(pluginClassLoader);
    configPostProcessor.setEnvironment(aCtx.getEnvironment());
    DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
    resourceLoader.setClassLoader(pluginClassLoader);
    configPostProcessor.setResourceLoader(resourceLoader);
    ctx.addBeanFactoryPostProcessor(configPostProcessor);

    // Reflect primary beans into the main bean factory
    ctx.getBeanFactory()
        .addBeanPostProcessor(
            new BeanPostProcessor() {
              @Override
              public Object postProcessAfterInitialization(Object bean, String beanName)
                  throws BeansException {
                try {
                  log.trace("Registering plugin bean {} into application context", beanName);

                  BeanDefinition def = ctx.getBeanDefinition(beanName);

                  if (def.isPrimary()) {
                    Class klass = bean.getClass();
                    if (def.getBeanClassName() != null) {
                      klass = pluginClassLoader.loadClass(def.getBeanClassName());
                    }
                    log.debug("Adding bean {} to application context", beanName);
                    aCtx.registerBean(beanName, klass, () -> bean, b -> b.setPrimary(true));
                  }
                } catch (ClassNotFoundException e) {
                  log.error("Error loading class for bean {}", beanName, e);
                }
                return bean;
              }
            });

    ctx.register(PluginConfiguration.class);
    ctx.refresh();
  }
}
