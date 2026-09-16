package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.consent.ConsentService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.action.api.Patchable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.UserConsentRequest;
import io.fusionauth.domain.api.UserConsentResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userConsentId}", requiresAuthentication = true, scheme = {"api"})
public class ConsentAction extends BaseTenantAPIAction implements Patchable {
  private final ConsentService consentService;
  
  @JSONPatch
  @JSONRequest
  public UserConsentRequest request = new UserConsentRequest();
  
  @JSONResponse
  public UserConsentResponse response;
  
  @PreParameter
  public UUID userConsentId;
  
  public UUID userId;
  
  @Inject
  public ConsentAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ConsentService paramConsentService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.consentService = paramConsentService;
  }
  
  public String delete() {
    this.consentService.revokeUserConsent(getOptionalTenantId(), this.userConsentId);
    return "success";
  }
  
  public String get() {
    if (this.userConsentId != null) {
      this.response = new UserConsentResponse(this.consentService.retrieveUserConsentById(getOptionalTenantId(), this.userConsentId));
      if (this.response.userConsent == null)
        return "missing"; 
    } else {
      this.response = new UserConsentResponse(this.consentService.retrieveUserConsentByUserId(getOptionalTenantId(), this.userId));
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.userConsentId != null)
      this.request.userConsent = this.consentService.retrieveUserConsentById(getOptionalTenantId(), this.userConsentId); 
  }
  
  public String post() {
    this.consentService.createUserConsent(this.request.userConsent, false);
    this.response = new UserConsentResponse(this.request.userConsent);
    return "render";
  }
  
  public String put() {
    this.response = new UserConsentResponse(this.consentService.updateUserConsent(getOptionalTenantId(), this.request.userConsent));
    if (this.response.userConsent == null)
      return "missing"; 
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT", "PATCH"})
  public void validate() {
    if (this.request.userConsent == null) {
      this.frontEndSupport.addFieldError("userConsent", "[missing]userConsent", new Object[0]);
      return;
    } 
    this.request.userConsent.id = this.userConsentId;
    this.frontEndSupport.transfer(this.consentService.validateUserConsent(getOptionalTenantId(), this.request.userConsent, this.frontEndSupport.isPOST()));
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.userConsentId == null)
      this.frontEndSupport.addFieldError("userConsentId", "[missing]userConsentId", new Object[0]); 
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.userConsentId == null && this.userId == null)
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]); 
  }
}
