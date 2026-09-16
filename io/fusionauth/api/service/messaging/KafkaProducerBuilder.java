package io.fusionauth.api.service.messaging;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaProducerBuilder {
  private final Map<String, String> properties;
  
  public KafkaProducerBuilder() {
    this.properties = new TreeMap<>();
  }
  
  public KafkaProducerBuilder(Map<String, String> paramMap) {
    this.properties = paramMap;
  }
  
  public KafkaProducer<String, String> build() {
    Properties properties = new Properties();
    properties.putAll(this.properties);
    return new KafkaProducer(properties, (Serializer)new StringSerializer(), (Serializer)new StringSerializer());
  }
  
  public KafkaProducerBuilder testMode() {
    this.properties.put("request.timeout.ms", "2000");
    return this;
  }
  
  public KafkaProducerBuilder withProperty(String paramString1, String paramString2) {
    this.properties.put(paramString1, paramString2);
    return this;
  }
}
