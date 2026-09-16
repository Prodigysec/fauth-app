package io.fusionauth.api.service.messaging;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class KafkaConsumerBuilder {
  private final Map<String, String> properties;
  
  public KafkaConsumerBuilder(Map<String, String> paramMap) {
    this.properties = new TreeMap<>();
    this.properties.putAll(paramMap);
  }
  
  public KafkaConsumerBuilder() {
    this.properties = new TreeMap<>();
  }
  
  public KafkaConsumer<String, String> build() {
    Properties properties = new Properties();
    properties.putAll(this.properties);
    return new KafkaConsumer(properties, (Deserializer)new StringDeserializer(), (Deserializer)new StringDeserializer());
  }
  
  public KafkaConsumerBuilder testMode() {
    this.properties.put("fetch.max.wait.ms", "500");
    this.properties.put("heartbeat.interval.ms", "500");
    this.properties.put("session.timeout.ms", "1000");
    this.properties.put("request.timeout.ms", "2000");
    return this;
  }
  
  public KafkaConsumerBuilder withProperty(String paramString1, String paramString2) {
    this.properties.put(paramString1, paramString2);
    return this;
  }
}
