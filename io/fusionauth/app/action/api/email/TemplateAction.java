package io.fusionauth.app.action.api.email;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.email.EmailTemplateService;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.action.api.Patchable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.EmailTemplateRequest;
import io.fusionauth.domain.api.EmailTemplateResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{emailTemplateId}", scheme = {"api-no-tenant"}, requiresAuthentication = true)
public class TemplateAction extends BaseAPIAction implements Patchable {
  private final EmailTemplateService emailTemplateService;
  
  @PreParameter
  public UUID emailTemplateId;
  
  @JSONPatch
  @JSONRequest
  public EmailTemplateRequest request = new EmailTemplateRequest();
  
  @JSONResponse
  public EmailTemplateResponse response;
  
  private EmailTemplateService.ValidationResult result;
  
  @Inject
  public TemplateAction(FrontEndSupport paramFrontEndSupport, EmailTemplateService paramEmailTemplateService) {
    super(paramFrontEndSupport);
    this.emailTemplateService = paramEmailTemplateService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.emailTemplateService.delete(this.emailTemplateId);
    return "success";
  }
  
  public String get() {
    if (this.emailTemplateId != null) {
      this.response = new EmailTemplateResponse(this.emailTemplateService.retrieveById(this.emailTemplateId));
      if (this.response.emailTemplate == null)
        return "missing"; 
    } else {
      this.response = new EmailTemplateResponse(this.emailTemplateService.retrieveAll());
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.emailTemplateId != null)
      this.request.emailTemplate = this.emailTemplateService.retrieveById(this.emailTemplateId); 
  }
  
  public String post() {
    this.emailTemplateService.create(this.request.emailTemplate);
    this.response = new EmailTemplateResponse(this.request.emailTemplate);
    return "render";
  }
  
  public String put() {
    if (!this.emailTemplateService.update(this.request.emailTemplate))
      return "missing"; 
    this.response = new EmailTemplateResponse(this.request.emailTemplate);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT", "PATCH"})
  public void validate() {
    if (this.request.emailTemplate == null) {
      this.frontEndSupport.addFieldError("emailTemplate", "[missing]emailTemplate", new Object[0]);
      return;
    } 
    this.request.emailTemplate.id = this.emailTemplateId;
    this.request.emailTemplate.normalize();
    Errors errors = this.emailTemplateService.validate(this.request.emailTemplate, this.frontEndSupport.isPOST());
    this.frontEndSupport.transfer(errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.emailTemplateId == null) {
      this.frontEndSupport.addFieldError("emailTemplateId", "[missing]emailTemplateId", new Object[0]);
    } else {
      this.result = this.emailTemplateService.validateDelete(this.emailTemplateId);
      this.frontEndSupport.transfer(this.result.errors);
    } 
  }
}
