package io.fusionauth.domain;

import com.inversoft.json.ToString;
import io.fusionauth.api.domain.json.annotation.MaskMapValue;
import io.fusionauth.api.domain.json.annotation.MaskMapValue.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class KafkaConfiguration extends Enableable implements Buildable<KafkaConfiguration>, Integration {
  public String defaultTopic;
  
  @List({@MaskMapValue("ssl.truststore.location"), @MaskMapValue("ssl.truststore.password"), @MaskMapValue("ssl.keystore.location"), @MaskMapValue("ssl.keystore.password"), @MaskMapValue("ssl.key.password"), @MaskMapValue("sasl.jaas.config")})
  public Map<String, String> producer = new TreeMap<>();
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    KafkaConfiguration kafkaConfiguration = (KafkaConfiguration)paramObject;
    return (super.equals(paramObject) && 
      Objects.equals(this.defaultTopic, kafkaConfiguration.defaultTopic) && 
      Objects.equals(this.producer, kafkaConfiguration.producer));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.defaultTopic, this.producer });
  }
  
  public void normalize() {
    if (!this.producer.containsKey("bootstrap.servers")) {
      this.producer.put("bootstrap.servers", "localhost:9092");
      this.producer.put("max.block.ms", "5000");
      this.producer.put("request.timeout.ms", "2000");
    } 
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
