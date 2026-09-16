package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.consent.ConsentService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.api.ConsentRequest;
import io.fusionauth.domain.api.ConsentResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{consentId}", requiresAuthentication = true, scheme = {"api"})
public class ConsentAction extends BaseTenantAPIAction implements Patchable {
  private final ConsentService consentService;
  
  @PreParameter
  public UUID consentId;
  
  @JSONPatch
  @JSONRequest
  public ConsentRequest request = new ConsentRequest();
  
  @JSONResponse
  public ConsentResponse response;
  
  private ConsentService.ValidationResult result;
  
  @Inject
  public ConsentAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ConsentService paramConsentService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.consentService = paramConsentService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.consentService.deleteConsent(this.result.existing);
    return "success";
  }
  
  public String get() {
    if (this.consentId == null) {
      this.response = new ConsentResponse(this.consentService.retrieveAllConsents());
    } else {
      Consent consent = this.consentService.retrieveConsentById(this.consentId);
      if (consent == null)
        return "missing"; 
      this.response = new ConsentResponse(consent);
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.consentId != null)
      this.request.consent = this.consentService.retrieveConsentById(this.consentId); 
  }
  
  public String post() {
    this.consentService.createConsent(this.request.consent);
    this.response = new ConsentResponse(this.request.consent);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.consentService.updateConsent(this.request.consent, this.result.existing);
    this.response = new ConsentResponse(this.request.consent);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT", "PATCH"})
  public void validate() {
    if (this.request == null || this.request.consent == null) {
      this.frontEndSupport.addFieldError("consent", "[missing]consent", new Object[0]);
      return;
    } 
    if (tenantIdWasSpecified()) {
      this.frontEndSupport.addGeneralError("[TenantScopeException]", new Object[0]);
      return;
    } 
    this.request.consent.id = this.consentId;
    this.request.consent.normalize();
    this.result = this.consentService.validateConsent(this.request.consent, this.frontEndSupport.isPOST());
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.consentId == null) {
      this.frontEndSupport.addFieldError("consentId", "[missing]consentId", new Object[0]);
      return;
    } 
    if (tenantIdWasSpecified()) {
      this.frontEndSupport.addGeneralError("[TenantScopeException]", new Object[0]);
      return;
    } 
    this.result = this.consentService.validateDelete(this.consentId);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
