package io.fusionauth.app.action.api.form;

import com.google.inject.Inject;
import io.fusionauth.api.service.form.FormService;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.action.api.Patchable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.FormFieldRequest;
import io.fusionauth.domain.api.FormFieldResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{fieldId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class FieldAction extends BaseAPIAction implements Patchable {
  private final FormService formService;
  
  @PreParameter
  public UUID fieldId;
  
  @JSONPatch
  @JSONRequest
  public FormFieldRequest request = new FormFieldRequest();
  
  @JSONResponse
  public FormFieldResponse response;
  
  private FormService.FieldValidationResult result;
  
  @Inject
  public FieldAction(FrontEndSupport paramFrontEndSupport, FormService paramFormService) {
    super(paramFrontEndSupport);
    this.formService = paramFormService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.formService.deleteField(this.result.existing);
    return "success";
  }
  
  public String get() {
    if (this.fieldId == null) {
      this.response = new FormFieldResponse(this.formService.retrieveAllFields());
    } else {
      this.response = new FormFieldResponse(this.formService.retrieveFieldById(this.fieldId));
      if (this.response.field == null)
        return "missing"; 
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.fieldId != null)
      this.request.field = this.formService.retrieveFieldById(this.fieldId); 
  }
  
  public String post() {
    this.formService.createField(this.request.field);
    this.response = new FormFieldResponse(this.request.field);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.formService.updateField(this.result.existing, this.request.field);
    this.response = new FormFieldResponse(this.request.field);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.fieldId == null) {
      this.frontEndSupport.addFieldError("fieldId", "[missing]fieldId", new Object[0]);
      return;
    } 
    this.result = this.formService.validateFieldDelete(this.fieldId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.field == null) {
      this.frontEndSupport.addFieldError("field", "[missing]field", new Object[0]);
      return;
    } 
    this.request.field.id = this.fieldId;
    this.request.field.normalize();
    this.result = this.formService.validateFieldCreate(this.request.field);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.request.field == null) {
      this.frontEndSupport.addFieldError("field", "[missing]field", new Object[0]);
      return;
    } 
    this.request.field.id = this.fieldId;
    this.request.field.normalize();
    this.result = this.formService.validateFieldUpdate(this.request.field);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
