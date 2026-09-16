package io.fusionauth.app.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.app.primeframework.FusionAuthObjectMapperProvider;
import org.primeframework.mvc.content.guice.ContentHandlerFactory;
import org.primeframework.mvc.content.guice.ContentModule;
import org.primeframework.mvc.content.json.JacksonContentHandler;

public class FusionAuthContentModule extends ContentModule {
  protected void bindContentHandlers() {
    super.bindContentHandlers();
    ContentHandlerFactory.addContentHandler(binder(), "application/scim+json", JacksonContentHandler.class);
  }
  
  protected void bindObjectMapper() {
    bind(ObjectMapper.class).toProvider(FusionAuthObjectMapperProvider.class).asEagerSingleton();
  }
}
