package io.fusionauth.api.service.messaging;

import io.fusionauth.api.service.event.EventSenderResult;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.KafkaConfiguration;
import org.apache.kafka.clients.producer.KafkaProducer;

public interface KafkaService {
  KafkaConfiguration getConfiguration();
  
  boolean isEnabled();
  
  <T extends io.fusionauth.domain.event.BaseEvent> EventSenderResult sendEvent(T paramT);
  
  KafkaSendResult sendMessage(KafkaProducer<String, String> paramKafkaProducer, String paramString1, String paramString2);
  
  <T extends io.fusionauth.domain.event.BaseEvent> void testConfiguration(KafkaConfiguration paramKafkaConfiguration, T paramT);
  
  public static class KafkaErrorEventLog extends EventLog {
    public KafkaErrorEventLog(EventLogType param1EventLogType, String param1String, Throwable param1Throwable) {
      super(param1EventLogType, param1String, param1Throwable);
    }
  }
  
  public static class KafkaSendResult {
    public Exception exception;
    
    public String message;
    
    public boolean success;
  }
}
