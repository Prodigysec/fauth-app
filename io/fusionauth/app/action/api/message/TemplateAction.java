package io.fusionauth.app.action.api.message;

import com.google.inject.Inject;
import io.fusionauth.api.service.message.MessageTemplateService;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.action.api.Patchable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.MessageTemplateRequest;
import io.fusionauth.domain.api.MessageTemplateResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{messageTemplateId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class TemplateAction extends BaseAPIAction implements Patchable {
  private final MessageTemplateService messageTemplateService;
  
  @PreParameter
  public UUID messageTemplateId;
  
  @JSONPatch
  @JSONRequest
  public MessageTemplateRequest request = new MessageTemplateRequest();
  
  @JSONResponse
  public MessageTemplateResponse response = new MessageTemplateResponse();
  
  private MessageTemplateService.ValidationResult result;
  
  @Inject
  public TemplateAction(FrontEndSupport paramFrontEndSupport, MessageTemplateService paramMessageTemplateService) {
    super(paramFrontEndSupport);
    this.messageTemplateService = paramMessageTemplateService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.messageTemplateService.delete(this.result.existing);
    return "success";
  }
  
  public String get() {
    if (this.messageTemplateId != null) {
      this.response = new MessageTemplateResponse(this.messageTemplateService.retrieveById(this.messageTemplateId));
      if (this.response.messageTemplate == null)
        return "missing"; 
    } else {
      this.response = new MessageTemplateResponse(this.messageTemplateService.retrieveAll());
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.messageTemplateId != null)
      this.request.messageTemplate = this.messageTemplateService.retrieveById(this.messageTemplateId); 
  }
  
  public String post() {
    this.messageTemplateService.create(this.request.messageTemplate);
    this.response = new MessageTemplateResponse(this.request.messageTemplate);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.messageTemplateService.update(this.result.existing, this.request.messageTemplate);
    this.response = new MessageTemplateResponse(this.request.messageTemplate);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validateCreate() {
    if (this.request.messageTemplate == null) {
      this.frontEndSupport.addFieldError("messageTemplate", "[missing]messageTemplate", new Object[0]);
      return;
    } 
    this.request.messageTemplate.id = this.messageTemplateId;
    this.request.messageTemplate.normalize();
    this.result = this.messageTemplateService.validateCreate(this.request.messageTemplate);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.messageTemplateId == null) {
      this.frontEndSupport.addFieldError("messageTemplateId", "[missing]messageTemplateId", new Object[0]);
      return;
    } 
    this.result = this.messageTemplateService.validateDelete(this.messageTemplateId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validateUpdate() {
    if (this.request.messageTemplate == null) {
      this.frontEndSupport.addFieldError("messageTemplate", "[missing]messageTemplate", new Object[0]);
      return;
    } 
    this.request.messageTemplate.id = this.messageTemplateId;
    this.request.messageTemplate.normalize();
    this.result = this.messageTemplateService.validateUpdate(this.request.messageTemplate);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
