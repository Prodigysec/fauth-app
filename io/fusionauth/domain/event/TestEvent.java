package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class TestEvent extends BaseEvent implements Buildable<TestEvent> {
  public String message;
  
  @JacksonConstructor
  public TestEvent() {
    this.message = "Example FusionAuth Event.";
  }
  
  public TestEvent(String paramString) {
    this.message = paramString;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    TestEvent testEvent = (TestEvent)paramObject;
    return Objects.equals(this.message, testEvent.message);
  }
  
  public EventType getType() {
    return EventType.Test;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.message });
  }
}
