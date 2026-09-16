package io.fusionauth.api.plugin.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import io.fusionauth.api.plugin.PluginClassLoader;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.primeframework.mvc.util.ClassClasspathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginModule extends AbstractModule {
  private static final Logger logger = LoggerFactory.getLogger(PluginModule.class);
  
  protected void configure() {
    List<PluginClassLoader> list = PluginClassLoader.makePluginClassLoader(PluginModule.class.getClassLoader());
    if (list.size() == 0) {
      logger.info("No plugins found");
      return;
    } 
    Multibinder multibinder = Multibinder.newSetBinder(binder(), ClassLoader.class);
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try {
      for (PluginClassLoader pluginClassLoader : list) {
        multibinder.addBinding().toInstance(pluginClassLoader);
        Thread.currentThread().setContextClassLoader(pluginClassLoader);
        ClassClasspathResolver classClasspathResolver = new ClassClasspathResolver();
        try {
          Set set = classClasspathResolver.findByLocators((ClassClasspathResolver.Test)new ClassClasspathResolver.AnnotatedWith(io.fusionauth.plugin.spi.PluginModule.class), true, new String[] { "plugin", "plugins" });
          if (set.isEmpty())
            continue; 
          for (Class<Module> clazz : (Iterable<Class<Module>>)set) {
            logger.info("Installing plugin [{}]", clazz.getCanonicalName());
            Module module = clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            install(module);
            logger.info("Plugin successfully installed");
          } 
        } catch (IOException|IllegalAccessException|InstantiationException|NoSuchMethodException|java.lang.reflect.InvocationTargetException iOException) {
          throw new RuntimeException("Error discovering plugin modules", iOException);
        } 
      } 
    } finally {
      Thread.currentThread().setContextClassLoader(classLoader);
    } 
  }
}
