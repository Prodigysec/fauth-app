package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import io.fusionauth.api.service.messenger.MessengerConfigurationService;
import io.fusionauth.api.service.messenger.MessengerValidator;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.MessengerRequest;
import io.fusionauth.domain.api.MessengerResponse;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.MessengerType;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{messengerId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class MessengerAction extends BaseAPIAction implements Patchable {
  private final MessengerConfigurationService messengerConfigurationService;
  
  private final Map<MessengerType, MessengerValidator> validators;
  
  @PreParameter
  public UUID messengerId;
  
  @JSONPatch
  @JSONRequest
  public MessengerRequest request = new MessengerRequest();
  
  @JSONResponse
  public MessengerResponse response;
  
  @PreParameter
  public MessengerType type;
  
  private MessengerConfigurationService.ValidationResult result;
  
  @Inject
  public MessengerAction(FrontEndSupport paramFrontEndSupport, MessengerConfigurationService paramMessengerConfigurationService, Map<MessengerType, MessengerValidator> paramMap) {
    super(paramFrontEndSupport);
    this.messengerConfigurationService = paramMessengerConfigurationService;
    this.validators = paramMap;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.messengerConfigurationService.delete(this.result.existing);
    return "success";
  }
  
  public String get() {
    if (this.messengerId != null) {
      BaseMessengerConfiguration baseMessengerConfiguration = this.messengerConfigurationService.retrieveById(this.messengerId);
      if (baseMessengerConfiguration == null)
        return "missing"; 
      this.response = new MessengerResponse(baseMessengerConfiguration);
    } else {
      this.response = new MessengerResponse(this.messengerConfigurationService.retrieveAll());
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.messengerId != null)
      this.request.messenger = this.messengerConfigurationService.retrieveById(this.messengerId); 
  }
  
  public String post() {
    this.messengerConfigurationService.create(this.request.messenger);
    this.response = new MessengerResponse(this.request.messenger);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.messengerConfigurationService.update(this.result.existing, this.result.messenger);
    this.response = new MessengerResponse(this.result.messenger);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.messengerId == null) {
      this.frontEndSupport.addFieldError("messengerId", "[missing]messengerId", new Object[0]);
      return;
    } 
    this.result = this.messengerConfigurationService.validateDelete(this.messengerId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.messenger == null) {
      this.frontEndSupport.addFieldError("messenger", "[missing]messenger", new Object[0]);
      return;
    } 
    this.request.messenger.id = this.messengerId;
    this.result = this.messengerConfigurationService.validateCreate(this.request.messenger);
    this.request.messenger.normalize();
    validateTypeSpecificParameters();
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.request.messenger == null) {
      this.frontEndSupport.addFieldError("messenger", "[missing]messenger", new Object[0]);
      return;
    } 
    this.request.messenger.id = this.messengerId;
    this.result = this.messengerConfigurationService.validateUpdate(this.request.messenger);
    this.request.messenger.normalize();
    validateTypeSpecificParameters();
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  private void validateTypeSpecificParameters() {
    MessengerValidator messengerValidator = this.validators.get(this.request.messenger.getType());
    if (messengerValidator != null)
      this.result.errors.add(messengerValidator.validate(this.request.messenger)); 
  }
}
