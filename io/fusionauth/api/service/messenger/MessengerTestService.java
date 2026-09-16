package io.fusionauth.api.service.messenger;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.messenger.GenericMessengerConfiguration;
import io.fusionauth.domain.messenger.KafkaMessengerConfiguration;
import io.fusionauth.domain.messenger.TwilioMessengerConfiguration;

public interface MessengerTestService {
  MessengerTestResult testGeneric(GenericMessengerConfiguration paramGenericMessengerConfiguration);
  
  MessengerTestResult testKafka(KafkaMessengerConfiguration paramKafkaMessengerConfiguration);
  
  MessengerTestResult testTwilio(TwilioMessengerConfiguration paramTwilioMessengerConfiguration, String paramString);
  
  public static class MessengerTestResult implements Buildable<MessengerTestResult> {
    public Throwable exception;
    
    public String message;
    
    public Integer status;
    
    public boolean success;
  }
}
