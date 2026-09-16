package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import java.util.Objects;
import java.util.UUID;

public class WebhookSignatureConfiguration extends Enableable implements Buildable<WebhookSignatureConfiguration> {
  @ExcludeFromJSONColumn
  public UUID signingKeyId;
  
  @JacksonConstructor
  public WebhookSignatureConfiguration() {}
  
  public WebhookSignatureConfiguration(WebhookSignatureConfiguration paramWebhookSignatureConfiguration) {
    this.enabled = paramWebhookSignatureConfiguration.enabled;
    this.signingKeyId = paramWebhookSignatureConfiguration.signingKeyId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof WebhookSignatureConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    WebhookSignatureConfiguration webhookSignatureConfiguration = (WebhookSignatureConfiguration)paramObject;
    return Objects.equals(this.signingKeyId, webhookSignatureConfiguration.signingKeyId);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.signingKeyId });
  }
}
