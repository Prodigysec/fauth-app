package io.fusionauth.domain;

import com.inversoft.json.ToString;
import java.net.URI;
import java.util.Objects;

public class WebhookCallResponse implements Buildable<WebhookCallResponse> {
  public String exception;
  
  public int statusCode;
  
  public URI url;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    WebhookCallResponse webhookCallResponse = (WebhookCallResponse)paramObject;
    return (this.statusCode == webhookCallResponse.statusCode && 
      Objects.equals(this.exception, webhookCallResponse.exception) && 
      Objects.equals(this.url, webhookCallResponse.url));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.exception, Integer.valueOf(this.statusCode), this.url });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
