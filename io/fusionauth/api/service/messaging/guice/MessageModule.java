package io.fusionauth.api.service.messaging.guice;

import com.google.inject.AbstractModule;
import io.fusionauth.api.service.message.DatabaseMessageTemplateLoader;
import io.fusionauth.api.service.message.DefaultMessageRenderer;
import io.fusionauth.api.service.message.DefaultMessageTemplateService;
import io.fusionauth.api.service.message.MessageRenderer;
import io.fusionauth.api.service.message.MessageTemplateLoader;
import io.fusionauth.api.service.message.MessageTemplateService;

public class MessageModule extends AbstractModule {
  protected void configure() {
    bind(MessageRenderer.class).to(DefaultMessageRenderer.class);
    bind(MessageTemplateService.class).to(DefaultMessageTemplateService.class);
    bind(MessageTemplateLoader.class).to(DatabaseMessageTemplateLoader.class);
  }
}
