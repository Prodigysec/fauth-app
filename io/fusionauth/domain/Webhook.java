package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.api.domain.json.annotation.MaskMapValue;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.event.EventType;
import io.fusionauth.domain.util.Normalizer;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Webhook implements Buildable<Webhook>, JSONColumnable {
  public Integer connectTimeout;
  
  public Map<String, Object> data = new LinkedHashMap<>();
  
  public String description;
  
  @JSONColumn
  public Map<EventType, Boolean> eventsEnabled = new HashMap<>();
  
  public boolean global;
  
  @MaskMapValue(maskAll = true)
  public HTTPHeaders headers = new HTTPHeaders();
  
  @MaskString
  public String httpAuthenticationPassword;
  
  public String httpAuthenticationUsername;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public Integer readTimeout;
  
  @JSONColumn
  public WebhookSignatureConfiguration signatureConfiguration = new WebhookSignatureConfiguration();
  
  @Deprecated
  public String sslCertificate;
  
  public UUID sslCertificateKeyId;
  
  public List<UUID> tenantIds = new ArrayList<>();
  
  public URI url;
  
  @JacksonConstructor
  public Webhook() {}
  
  public Webhook(Webhook paramWebhook) {
    this.connectTimeout = paramWebhook.connectTimeout;
    this.data.putAll(paramWebhook.data);
    this.description = paramWebhook.description;
    this.eventsEnabled.putAll(paramWebhook.eventsEnabled);
    this.global = paramWebhook.global;
    this.headers.putAll(paramWebhook.headers);
    this.httpAuthenticationPassword = paramWebhook.httpAuthenticationPassword;
    this.httpAuthenticationUsername = paramWebhook.httpAuthenticationUsername;
    this.id = paramWebhook.id;
    this.insertInstant = paramWebhook.insertInstant;
    this.lastUpdateInstant = paramWebhook.lastUpdateInstant;
    this.readTimeout = paramWebhook.readTimeout;
    this.signatureConfiguration = new WebhookSignatureConfiguration(paramWebhook.signatureConfiguration);
    this.sslCertificate = paramWebhook.sslCertificate;
    this.sslCertificateKeyId = paramWebhook.sslCertificateKeyId;
    this.tenantIds.addAll(paramWebhook.tenantIds);
    this.url = paramWebhook.url;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Webhook))
      return false; 
    Webhook webhook = (Webhook)paramObject;
    return (this.global == webhook.global && 
      Objects.equals(this.connectTimeout, webhook.connectTimeout) && 
      Objects.equals(this.data, webhook.data) && Objects.equals(this.description, webhook.description) && 
      Objects.equals(this.eventsEnabled, webhook.eventsEnabled) && Objects.equals(this.headers, webhook.headers) && 
      Objects.equals(this.httpAuthenticationPassword, webhook.httpAuthenticationPassword) && 
      Objects.equals(this.httpAuthenticationUsername, webhook.httpAuthenticationUsername) && 
      Objects.equals(this.id, webhook.id) && Objects.equals(this.insertInstant, webhook.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, webhook.lastUpdateInstant) && 
      Objects.equals(this.readTimeout, webhook.readTimeout) && 
      Objects.equals(this.signatureConfiguration, webhook.signatureConfiguration) && 
      Objects.equals(this.sslCertificate, webhook.sslCertificate) && 
      Objects.equals(this.sslCertificateKeyId, webhook.sslCertificateKeyId) && 
      Objects.equals(this.tenantIds, webhook.tenantIds) && 
      Objects.equals(this.url, webhook.url));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.connectTimeout, this.data, this.description, this.eventsEnabled, Boolean.valueOf(this.global), this.headers, this.httpAuthenticationPassword, this.httpAuthenticationUsername, this.id, this.insertInstant, 
          this.lastUpdateInstant, this.readTimeout, this.signatureConfiguration, this.sslCertificate, this.sslCertificateKeyId, this.tenantIds, this.url });
  }
  
  public void normalize() {
    this.headers.normalize();
    this.httpAuthenticationPassword = Normalizer.trim(this.httpAuthenticationPassword);
    this.httpAuthenticationUsername = Normalizer.trim(this.httpAuthenticationUsername);
    this.sslCertificate = Normalizer.trim(this.sslCertificate);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
