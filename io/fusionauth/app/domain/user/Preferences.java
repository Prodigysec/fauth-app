package io.fusionauth.app.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Preferences implements Buildable<Preferences> {
  public Map<Object, Object> announcements = new HashMap<>();
  
  public boolean equals(Object paramObject) {
    Preferences preferences;
    if (this == paramObject)
      return true; 
    if (paramObject instanceof Preferences) {
      preferences = (Preferences)paramObject;
    } else {
      return false;
    } 
    return Objects.equals(this.announcements, preferences.announcements);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.announcements });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
