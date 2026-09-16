package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class WebhookEventLogConfiguration extends Enableable {
  public SystemConfiguration.DeleteConfiguration delete = new SystemConfiguration.DeleteConfiguration(30, true);
  
  @JacksonConstructor
  public WebhookEventLogConfiguration() {}
  
  public WebhookEventLogConfiguration(WebhookEventLogConfiguration paramWebhookEventLogConfiguration) {
    this.enabled = paramWebhookEventLogConfiguration.enabled;
    this.delete = new SystemConfiguration.DeleteConfiguration(paramWebhookEventLogConfiguration.delete);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof WebhookEventLogConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    WebhookEventLogConfiguration webhookEventLogConfiguration = (WebhookEventLogConfiguration)paramObject;
    return Objects.equals(this.delete, webhookEventLogConfiguration.delete);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.delete });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
