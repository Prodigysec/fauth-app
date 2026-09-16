package io.fusionauth.app.action.api.system;

import com.google.inject.Inject;
import io.fusionauth.api.service.event.WebhookEventLogService;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.WebhookEventLogResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{id}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class WebhookEventLogAction extends BaseAPIAction {
  private final WebhookEventLogService webhookEventLogService;
  
  public UUID id;
  
  @JSONResponse
  public WebhookEventLogResponse response;
  
  private WebhookEventLogService.ValidationResult result;
  
  @Inject
  public WebhookEventLogAction(FrontEndSupport paramFrontEndSupport, WebhookEventLogService paramWebhookEventLogService) {
    super(paramFrontEndSupport);
    this.webhookEventLogService = paramWebhookEventLogService;
  }
  
  public String get() {
    if (this.result.webhookEventLog == null)
      return "missing"; 
    this.response = new WebhookEventLogResponse(this.result.webhookEventLog);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.result = this.webhookEventLogService.validateRetrieveWebhookEventLogById(this.id);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
