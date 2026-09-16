package io.fusionauth.api.service.messenger.kafka;

import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.messenger.MessengerValidator;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.KafkaMessengerConfiguration;
import java.util.Set;

public class KafkaMessengerValidator implements MessengerValidator {
  public Errors validate(BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    KafkaMessengerConfiguration kafkaMessengerConfiguration = (KafkaMessengerConfiguration)paramBaseMessengerConfiguration;
    return (new Validator())
      
      .notBlank(kafkaMessengerConfiguration.defaultTopic, "messenger.defaultTopic", new Object[0])



      
      .notEmpty(kafkaMessengerConfiguration.producer.keySet(), "messenger.producer", new Object[0])

      
      .ensure((kafkaMessengerConfiguration.messageTypes == null || kafkaMessengerConfiguration.messageTypes.equals(Set.of(MessageType.SMS))), "messenger.messageTypes", "[invalid]", new Object[0])

      
      .done();
  }
}
