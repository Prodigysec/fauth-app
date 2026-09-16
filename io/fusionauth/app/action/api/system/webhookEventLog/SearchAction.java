package io.fusionauth.app.action.api.system.webhookEventLog;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.event.WebhookEventLogService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.WebhookEventResult;
import io.fusionauth.domain.api.WebhookEventLogSearchRequest;
import io.fusionauth.domain.api.WebhookEventLogSearchResponse;
import io.fusionauth.domain.event.EventType;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.WebhookEventLogSearchCriteria;
import java.time.ZonedDateTime;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class SearchAction extends BaseSearchAPIAction<WebhookEventLogSearchCriteria> {
  @JSONRequest
  public final WebhookEventLogSearchRequest request = new WebhookEventLogSearchRequest();
  
  private final WebhookEventLogService webhookEventLogService;
  
  @JSONResponse
  public WebhookEventLogSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, WebhookEventLogService paramWebhookEventLogService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.webhookEventLogService = paramWebhookEventLogService;
  }
  
  public ZonedDateTime end() {
    return (criteria()).end;
  }
  
  public String event() {
    return (criteria()).event;
  }
  
  public WebhookEventResult eventResult() {
    return (criteria()).eventResult;
  }
  
  public EventType eventType() {
    return (criteria()).eventType;
  }
  
  public void setEnd(ZonedDateTime paramZonedDateTime) {
    (criteria()).end = paramZonedDateTime;
  }
  
  public void setEvent(String paramString) {
    (criteria()).event = paramString;
  }
  
  public void setEventResult(WebhookEventResult paramWebhookEventResult) {
    (criteria()).eventResult = paramWebhookEventResult;
  }
  
  public void setEventType(String paramString) {
    (criteria()).eventType = EventType.forValue(paramString);
  }
  
  public void setStart(ZonedDateTime paramZonedDateTime) {
    (criteria()).start = paramZonedDateTime;
  }
  
  public ZonedDateTime start() {
    return (criteria()).start;
  }
  
  protected WebhookEventLogSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    this.response = new WebhookEventLogSearchResponse(this.webhookEventLogService.searchWebhookEventLog(this.request.search));
    return "render";
  }
}
