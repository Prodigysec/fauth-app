package io.fusionauth.app.action.api.key;

import com.google.inject.Inject;
import io.fusionauth.api.service.system.KeyService;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.KeyRequest;
import io.fusionauth.domain.api.KeyResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{keyId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class GenerateAction extends BaseAPIAction {
  private final KeyService keyService;
  
  public UUID keyId;
  
  @JSONRequest
  public KeyRequest request = new KeyRequest();
  
  @JSONResponse
  public KeyResponse response;
  
  @Inject
  public GenerateAction(FrontEndSupport paramFrontEndSupport, KeyService paramKeyService) {
    super(paramFrontEndSupport);
    this.keyService = paramKeyService;
  }
  
  public String post() {
    this.keyService.create(this.request.key);
    this.response = new KeyResponse(this.request.key.secure());
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.request == null || this.request.key == null) {
      this.frontEndSupport.addFieldError("key", "[missing]key", new Object[0]);
      return;
    } 
    this.request.key.id = this.keyId;
    this.request.key.kid = null;
    KeyService.ValidationResult validationResult = this.keyService.validate(this.request.key, true);
    this.frontEndSupport.transfer(validationResult.errors);
  }
}
