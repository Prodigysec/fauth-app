package io.fusionauth.domain.messenger;

import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.domain.Buildable;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class KafkaMessengerConfiguration extends BaseMessengerConfiguration implements Buildable<KafkaMessengerConfiguration> {
  @JSONColumn
  public String defaultTopic;
  
  @JSONColumn
  public Map<String, String> producer = new TreeMap<>();
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    KafkaMessengerConfiguration kafkaMessengerConfiguration = (KafkaMessengerConfiguration)paramObject;
    return (Objects.equals(this.defaultTopic, kafkaMessengerConfiguration.defaultTopic) && 
      Objects.equals(this.producer, kafkaMessengerConfiguration.producer));
  }
  
  public MessengerType getType() {
    return MessengerType.Kafka;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.defaultTopic, this.producer });
  }
  
  public void normalize() {
    super.normalize();
    if (!this.producer.containsKey("bootstrap.servers")) {
      this.producer.put("bootstrap.servers", "localhost:9092");
      this.producer.put("max.block.ms", "5000");
      this.producer.put("request.timeout.ms", "2000");
    } 
  }
}
