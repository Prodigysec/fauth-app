package io.fusionauth.api.service.email;

import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import freemarker.core.Environment;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.concurrent.ExecutorService;
import org.primeframework.email.config.DefaultEmailConfiguration;
import org.primeframework.email.config.EmailConfiguration;
import org.primeframework.email.guice.Email;
import org.primeframework.email.guice.EmailModule;
import org.primeframework.email.service.EmailRenderer;
import org.primeframework.email.service.EmailService;
import org.primeframework.email.service.EmailTemplateLoader;
import org.primeframework.email.service.EmailTransportService;
import org.primeframework.email.service.FreeMarkerEmailRenderer;
import org.primeframework.email.service.JavaMailEmailTransportService;
import org.primeframework.email.service.JavaMailSessionProvider;
import org.primeframework.email.service.MessagingExceptionHandler;

public class FusionAuthEmailModule extends EmailModule {
  @Provides
  @Singleton
  @Email
  public Configuration freeMarkerConfiguration() {
    DefaultObjectWrapper defaultObjectWrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_34);
    defaultObjectWrapper.setExposeFields(true);
    Configuration configuration = new Configuration(Configuration.VERSION_2_3_34);
    configuration.setObjectWrapper((ObjectWrapper)defaultObjectWrapper);
    configuration.setTagSyntax(2);
    configuration.setDefaultEncoding("UTF-8");
    configuration.setNumberFormat("computer");
    configuration.setLogTemplateExceptions(false);
    configuration.setTemplateExceptionHandler((paramTemplateException, paramEnvironment, paramWriter) -> {
          if (paramTemplateException instanceof freemarker.core.NonNumericalException || paramTemplateException instanceof freemarker.core.NonHashException || paramTemplateException instanceof freemarker.core.NonStringOrTemplateOutputException) {
            PrintWriter printWriter = (paramWriter instanceof PrintWriter) ? (PrintWriter)paramWriter : new PrintWriter(paramWriter);
            printWriter.write("{{" + paramTemplateException.getBlamedExpressionString() + "}}");
            return;
          } 
          throw paramTemplateException;
        });
    configuration.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
    configuration.setAPIBuiltinEnabled(false);
    return configuration;
  }
  
  protected void bindMessagingExceptionHandler() {
    bind(MessagingExceptionHandler.class).to(FusionAuthMessagingExceptionHandler.class);
  }
  
  protected void bindSessionProvider() {
    bind(JavaMailSessionProvider.class).to(FusionAuthJavaMailSessionProvider.class);
  }
  
  protected void bindTemplateLoader() {
    bind(EmailTemplateLoader.class).to(DatabaseEmailTemplateLoader.class);
  }
  
  protected void configure() {
    bind(EmailConfiguration.class).to(DefaultEmailConfiguration.class);
    bind(EmailService.class).to(FusionAuthEmailService.class);
    bind(EmailRenderer.class).to(FreeMarkerEmailRenderer.class);
    bind(EmailTransportService.class).to(JavaMailEmailTransportService.class);
    bind(FusionAuthEmailExecutorServiceProvider.class).in(Scopes.SINGLETON);
    bind(ExecutorService.class).annotatedWith((Annotation)Names.named("EmailExecutorService")).toProvider(FusionAuthEmailExecutorServiceProvider.class);
    bindSessionProvider();
    bindTemplateLoader();
    bindMessagingExceptionHandler();
  }
}
