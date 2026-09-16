package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.event.EventRequest;
import io.fusionauth.domain.event.EventType;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(value = {"successfulAttempts", "failedAttempts"}, allowGetters = true)
public class WebhookEventLog implements Buildable<WebhookEventLog>, JSONColumnable {
  @JsonIgnoreProperties({"webhookEventLogId"})
  public List<WebhookAttemptLog> attempts = new ArrayList<>();
  
  public Map<String, Object> data = new LinkedHashMap<>();
  
  @JSONColumn
  public EventRequest event;
  
  public WebhookEventResult eventResult = WebhookEventResult.Running;
  
  public EventType eventType;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastAttemptInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public UUID linkedObjectId;
  
  public Long sequence;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    WebhookEventLog webhookEventLog = (WebhookEventLog)paramObject;
    return (Objects.equals(this.attempts, webhookEventLog.attempts) && 
      Objects.equals(this.data, webhookEventLog.data) && 
      Objects.equals(this.event, webhookEventLog.event) && this.eventResult == webhookEventLog.eventResult && this.eventType == webhookEventLog.eventType && 

      
      Objects.equals(this.id, webhookEventLog.id) && 
      Objects.equals(this.insertInstant, webhookEventLog.insertInstant) && 
      Objects.equals(this.lastAttemptInstant, webhookEventLog.lastAttemptInstant) && 
      Objects.equals(this.lastUpdateInstant, webhookEventLog.lastUpdateInstant) && 
      Objects.equals(this.linkedObjectId, webhookEventLog.linkedObjectId) && 
      Objects.equals(this.sequence, webhookEventLog.sequence));
  }
  
  public Integer getFailedAttempts() {
    return Integer.valueOf(Math.toIntExact(this.attempts.stream()
          .filter(paramWebhookAttemptLog -> paramWebhookAttemptLog.getAttemptResult().equals(WebhookAttemptResult.Failure))
          .count()));
  }
  
  public Integer getSuccessfulAttempts() {
    return Integer.valueOf(Math.toIntExact(this.attempts.stream()
          .filter(paramWebhookAttemptLog -> paramWebhookAttemptLog.getAttemptResult().equals(WebhookAttemptResult.Success))
          .count()));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.attempts, this.data, this.event, this.eventResult, this.eventType, this.id, this.insertInstant, this.lastAttemptInstant, this.lastUpdateInstant, this.linkedObjectId, 
          this.sequence });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
