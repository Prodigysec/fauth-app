package io.fusionauth.api.service.messenger.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import io.fusionauth.api.service.messenger.DefaultMessengerConfigurationService;
import io.fusionauth.api.service.messenger.DefaultMessengerService;
import io.fusionauth.api.service.messenger.DefaultMessengerServiceProxy;
import io.fusionauth.api.service.messenger.DefaultMessengerTestService;
import io.fusionauth.api.service.messenger.MessengerConfigurationService;
import io.fusionauth.api.service.messenger.MessengerService;
import io.fusionauth.api.service.messenger.MessengerServiceProxy;
import io.fusionauth.api.service.messenger.MessengerTestService;
import io.fusionauth.api.service.messenger.MessengerValidator;
import io.fusionauth.api.service.messenger.generic.GenericMessengerValidator;
import io.fusionauth.api.service.messenger.kafka.KafkaMessengerValidator;
import io.fusionauth.api.service.messenger.twilio.TwilioMessengerValidator;
import io.fusionauth.domain.messenger.MessengerType;

public class MessengerModule extends AbstractModule {
  protected void configure() {
    MapBinder mapBinder = MapBinder.newMapBinder(binder(), MessengerType.class, MessengerValidator.class);
    mapBinder.addBinding(MessengerType.Twilio).to(TwilioMessengerValidator.class);
    mapBinder.addBinding(MessengerType.Kafka).to(KafkaMessengerValidator.class);
    mapBinder.addBinding(MessengerType.Generic).to(GenericMessengerValidator.class);
    bind(MessengerService.class).to(DefaultMessengerService.class);
    bind(MessengerServiceProxy.class).to(DefaultMessengerServiceProxy.class);
    bind(MessengerTestService.class).to(DefaultMessengerTestService.class);
    bind(MessengerConfigurationService.class).to(DefaultMessengerConfigurationService.class);
  }
}
