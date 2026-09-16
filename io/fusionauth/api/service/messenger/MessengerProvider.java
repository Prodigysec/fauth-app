package io.fusionauth.api.service.messenger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import io.fusionauth.api.service.messenger.kafka.KafkaMessenger;
import io.fusionauth.domain.messenger.MessengerType;

public class MessengerProvider implements Provider<Messenger> {
  private final Injector injector;
  
  @Inject
  public MessengerProvider(Injector paramInjector) {
    this.injector = paramInjector;
  }
  
  public Messenger get() {
    throw new UnsupportedOperationException("Please use the other get() method that takes a messengerType argument.");
  }
  
  public Messenger get(MessengerType paramMessengerType) {
    switch (paramMessengerType) {
      default:
        throw new MatchException(null, null);
      case Generic:
      
      case Twilio:
      
      case Kafka:
        break;
    } 
    return 

      
      (KafkaMessenger)this.injector.getInstance(KafkaMessenger.class);
  }
}
