package io.fusionauth.domain;

import com.inversoft.json.ToString;
import java.util.Objects;

public class Integrations implements Buildable<Integrations> {
  public CleanSpeakConfiguration cleanspeak = new CleanSpeakConfiguration();
  
  public KafkaConfiguration kafka = new KafkaConfiguration();
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    Integrations integrations = (Integrations)paramObject;
    return (Objects.equals(this.cleanspeak, integrations.cleanspeak) && 
      Objects.equals(this.kafka, integrations.kafka));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.cleanspeak, this.kafka });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
