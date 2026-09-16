package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import io.fusionauth.api.service.form.FormService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.FormRequest;
import io.fusionauth.domain.api.FormResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{formId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class FormAction extends BaseAPIAction implements Patchable {
  private final FormService formService;
  
  @PreParameter
  public UUID formId;
  
  @JSONPatch
  @JSONRequest
  public FormRequest request = new FormRequest();
  
  @JSONResponse
  public FormResponse response;
  
  private FormService.ValidationResult result;
  
  @Inject
  public FormAction(FrontEndSupport paramFrontEndSupport, FormService paramFormService) {
    super(paramFrontEndSupport);
    this.formService = paramFormService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.formService.delete(this.result.existing);
    return "success";
  }
  
  public String get() {
    if (this.formId == null) {
      this.response = new FormResponse(this.formService.retrieveAll());
    } else {
      this.response = new FormResponse(this.formService.retrieveById(this.formId));
      if (this.response.form == null)
        return "missing"; 
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.formId != null)
      this.request.form = this.formService.retrieveById(this.formId); 
  }
  
  public String post() {
    this.formService.create(this.request.form);
    this.response = new FormResponse(this.request.form);
    return "render";
  }
  
  public String put() throws Exception {
    if (this.result.existing == null)
      return "missing"; 
    this.formService.update(this.result.existing, this.request.form);
    this.response = new FormResponse(this.request.form);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.formId == null) {
      this.frontEndSupport.addFieldError("formId", "[missing]formId", new Object[0]);
      return;
    } 
    this.result = this.formService.validateDelete(this.formId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.form == null) {
      this.frontEndSupport.addFieldError("form", "[missing]form", new Object[0]);
      return;
    } 
    this.request.form.id = this.formId;
    this.request.form.normalize();
    this.result = this.formService.validateCreate(this.request.form);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.request.form == null) {
      this.frontEndSupport.addFieldError("form", "[missing]form", new Object[0]);
      return;
    } 
    this.request.form.id = this.formId;
    this.request.form.normalize();
    this.result = this.formService.validateUpdate(this.request.form);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
