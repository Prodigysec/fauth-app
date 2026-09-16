package io.fusionauth.api.service.messenger.kafka;

import com.google.inject.Inject;
import io.fusionauth.api.domain.message.SendMessageResult;
import io.fusionauth.api.service.messaging.KafkaProducerBuilder;
import io.fusionauth.api.service.messaging.KafkaService;
import io.fusionauth.api.service.messenger.Messenger;
import io.fusionauth.api.service.messenger.MessengerException;
import io.fusionauth.domain.message.Message;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.KafkaMessengerConfiguration;
import org.apache.kafka.clients.producer.KafkaProducer;

public class KafkaMessenger implements Messenger {
  private final KafkaService kafkaService;
  
  @Inject
  public KafkaMessenger(KafkaService paramKafkaService) {
    this.kafkaService = paramKafkaService;
  }
  
  public SendMessageResult send(Message paramMessage, BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    KafkaMessengerConfiguration kafkaMessengerConfiguration = (KafkaMessengerConfiguration)paramBaseMessengerConfiguration;
    KafkaProducer<String, String> kafkaProducer = (new KafkaProducerBuilder(kafkaMessengerConfiguration.producer)).build();
    KafkaService.KafkaSendResult kafkaSendResult = this.kafkaService.sendMessage(kafkaProducer, kafkaMessengerConfiguration.defaultTopic, "Hello World!");
    if (kafkaSendResult.exception != null)
      throw new MessengerException(kafkaMessengerConfiguration.name, null, kafkaSendResult.exception); 
    return new SendMessageResult(200);
  }
}
