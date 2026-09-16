package io.fusionauth.app.action.api.system;

import com.google.inject.Inject;
import io.fusionauth.api.service.event.WebhookEventLogService;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.WebhookAttemptLogResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{id}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class WebhookAttemptLogAction extends BaseAPIAction {
  private final WebhookEventLogService webhookEventLogService;
  
  public UUID id;
  
  @JSONResponse
  public WebhookAttemptLogResponse response;
  
  private WebhookEventLogService.ValidationResult result;
  
  @Inject
  public WebhookAttemptLogAction(FrontEndSupport paramFrontEndSupport, WebhookEventLogService paramWebhookEventLogService) {
    super(paramFrontEndSupport);
    this.webhookEventLogService = paramWebhookEventLogService;
  }
  
  public String get() {
    if (this.result.webhookAttemptLog == null)
      return "missing"; 
    this.response = new WebhookAttemptLogResponse(this.result.webhookAttemptLog);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.result = this.webhookEventLogService.validateRetrieveWebhookAttemptLogById(this.id);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
