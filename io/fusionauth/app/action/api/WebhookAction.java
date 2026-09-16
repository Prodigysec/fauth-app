package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import io.fusionauth.api.service.event.WebhookService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.api.WebhookRequest;
import io.fusionauth.domain.api.WebhookResponse;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{webhookId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class WebhookAction extends BaseAPIAction implements Patchable {
  private final WebhookService webhookService;
  
  @JSONPatch
  @JSONRequest
  public WebhookRequest request = new WebhookRequest();
  
  @JSONResponse
  public WebhookResponse response;
  
  @PreParameter
  public UUID webhookId;
  
  private WebhookService.ValidationResult result;
  
  @Inject
  public WebhookAction(FrontEndSupport paramFrontEndSupport, WebhookService paramWebhookService) {
    super(paramFrontEndSupport);
    this.webhookService = paramWebhookService;
  }
  
  public String delete() {
    if (this.webhookService.delete(this.webhookId) == 0)
      return "missing"; 
    return "success";
  }
  
  public String get() {
    if (this.webhookId == null) {
      List<Webhook> list = this.webhookService.retrieveAll();
      list.sort(Comparator.comparing(paramWebhook -> paramWebhook.url));
      this.response = new WebhookResponse(list);
      return "render";
    } 
    this.response = new WebhookResponse(this.webhookService.retrieveById(this.webhookId));
    if (this.response.webhook == null)
      return "missing"; 
    return "render";
  }
  
  public void loadExisting() {
    if (this.webhookId != null)
      this.request.webhook = this.webhookService.retrieveById(this.webhookId); 
  }
  
  public String post() {
    this.webhookService.create(this.request.webhook);
    this.response = new WebhookResponse(this.request.webhook);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.webhookService.update(this.result.existing, this.request.webhook);
    this.response = new WebhookResponse(this.request.webhook);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT", "PATCH"})
  public void validate() {
    if (this.request == null || this.request.webhook == null) {
      this.frontEndSupport.addFieldError("webhook", "[missing]webhook", new Object[0]);
      return;
    } 
    this.request.webhook.id = this.webhookId;
    this.request.webhook.normalize();
    this.result = this.webhookService.validate(this.request.webhook, this.frontEndSupport.isPOST());
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.webhookId == null)
      this.frontEndSupport.addFieldError("webhookId", "[missing]webhookId", new Object[0]); 
  }
}
