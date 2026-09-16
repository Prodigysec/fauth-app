package io.fusionauth.api.service.event;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONBodyHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import io.fusionauth.api.service.jwt.JWTHelper;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.util.HashTools;
import io.fusionauth.api.util.URITools;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.WebhookAttemptLog;
import io.fusionauth.domain.WebhookCallResponse;
import io.fusionauth.domain.event.BaseEvent;
import io.fusionauth.domain.event.EventRequest;
import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.domain.JWT;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class WebhookEventSender implements EventSender {
  public final Webhook webhook;
  
  public final WebhookEventLogService webhookEventLogService;
  
  private final MetricRegistry metricRegistry;
  
  private final ObjectMapper objectMapper;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  private final Key signingKey;
  
  public boolean logFailures = true;
  
  public WebhookEventSender(MetricRegistry paramMetricRegistry, ProxyInfoSupplier paramProxyInfoSupplier, Key paramKey, Webhook paramWebhook, WebhookEventLogService paramWebhookEventLogService, ObjectMapper paramObjectMapper) {
    this.metricRegistry = paramMetricRegistry;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
    this.signingKey = paramKey;
    this.webhook = paramWebhook;
    this.webhookEventLogService = paramWebhookEventLogService;
    this.objectMapper = paramObjectMapper;
  }
  
  public EventSenderResult send(BaseEvent paramBaseEvent) {
    ClientResponse clientResponse;
    ZonedDateTime zonedDateTime1, zonedDateTime2;
    EventSenderResult eventSenderResult = new EventSenderResult();
    eventSenderResult.sender = this;
    Map<String, List> map = (Map)this.webhook.headers.keySet().stream().collect(Collectors.toMap(paramString -> paramString, paramString -> List.of(this.webhook.headers.get(paramString))));
    JSONBodyHandler jSONBodyHandler = new JSONBodyHandler(new EventRequest(paramBaseEvent), this.objectMapper);
    if (this.signingKey != null)
      map.put("X-FusionAuth-Signature-JWT", List.of(jwtSignature(jSONBodyHandler.getBody(), this.signingKey))); 
    Timer timer1 = this.metricRegistry.timer("webhook.[*].requests");
    Timer timer2 = this.metricRegistry.timer("webhook.[" + String.valueOf(this.webhook.id) + "].requests");
    Meter meter1 = this.metricRegistry.meter("webhook.[*].failures");
    Meter meter2 = this.metricRegistry.meter("webhook.[" + String.valueOf(this.webhook.id) + "].failures");
    Timer.Context context = timer1.time();
    try {
      Timer.Context context1 = timer2.time();
      try {
        zonedDateTime1 = ZonedDateTime.now(ZoneOffset.UTC);
        clientResponse = (new RESTClient(void.class, void.class)).url(this.webhook.url.toString()).bodyHandler((RESTClient.BodyHandler)jSONBodyHandler).certificate(this.webhook.sslCertificate).basicAuthorization(this.webhook.httpAuthenticationUsername, this.webhook.httpAuthenticationPassword).connectTimeout(this.webhook.connectTimeout.intValue()).readTimeout(this.webhook.readTimeout.intValue()).proxy((ProxyInfo)this.proxyInfoSupplier.get()).headers(map).post().go();
        zonedDateTime2 = ZonedDateTime.now(ZoneOffset.UTC);
        if (context1 != null)
          context1.close(); 
      } catch (Throwable throwable) {
        if (context1 != null)
          try {
            context1.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
      if (context != null)
        context.close(); 
    } catch (Throwable throwable) {
      if (context != null)
        try {
          context.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
    eventSenderResult.success = clientResponse.wasSuccessful();
    eventSenderResult.status = clientResponse.getStatus();
    eventSenderResult.exception = clientResponse.exception;
    if (!eventSenderResult.success) {
      meter1.mark();
      meter2.mark();
      eventSenderResult.message = "Webhook [" + this.webhook.url.toString() + "] returned response code [" + clientResponse.status + "] when sending [" + String.valueOf(paramBaseEvent.getType()) + "] event with Id [" + String.valueOf(paramBaseEvent.id) + "].\n";
      if (this.logFailures) {
        boolean bool = !(paramBaseEvent instanceof io.fusionauth.domain.event.EventLogCreateEvent) ? true : false;
        if (clientResponse.exception != null) {
          EventLogHelper.create(new WebhookErrorEventLog(EventLogType.Error, eventSenderResult.message, clientResponse.exception, this.webhook.id), bool);
        } else {
          EventLogHelper.create(new WebhookErrorEventLog(EventLogType.Error, eventSenderResult.message + " Response: \n" + eventSenderResult.message, this.webhook.id), bool);
        } 
      } 
    } else {
      eventSenderResult.message = "OK";
    } 
    if (this.webhookEventLogService != null)
      this.webhookEventLogService.createWebhookAttemptLog(() -> (new WebhookAttemptLog()).with(()).with(()).with(()).with(()).with(()).with(())); 
    return eventSenderResult;
  }
  
  private String jwtSignature(byte[] paramArrayOfbyte, Key paramKey) {
    String str = HashTools.sha256(paramArrayOfbyte);
    JWT jWT = new JWT();
    jWT.addClaim("request_body_sha256", str);
    Signer signer = JWTHelper.buildSigner(paramKey);
    return JWT.getEncoder().encode(jWT, signer);
  }
  
  private String stackTraceToString(Throwable paramThrowable) {
    StringWriter stringWriter = new StringWriter();
    paramThrowable.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }
  
  static class WebhookErrorEventLog extends EventLog {
    public UUID webhookId;
    
    public WebhookErrorEventLog(EventLogType param1EventLogType, String param1String, UUID param1UUID) {
      super(param1EventLogType, param1String);
      this.webhookId = param1UUID;
    }
    
    public WebhookErrorEventLog(EventLogType param1EventLogType, String param1String, Throwable param1Throwable, UUID param1UUID) {
      super(param1EventLogType, param1String, param1Throwable);
      this.webhookId = param1UUID;
    }
  }
}
