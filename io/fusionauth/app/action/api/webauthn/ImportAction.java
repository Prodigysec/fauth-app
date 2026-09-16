package io.fusionauth.app.action.api.webauthn;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.webauthn.WebAuthnProviderService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.api.WebAuthnCredentialImportRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class ImportAction extends BaseWebAuthnAction {
  @JSONRequest
  public final WebAuthnCredentialImportRequest request = new WebAuthnCredentialImportRequest();
  
  private final WebAuthnProviderService webAuthnProviderService;
  
  @Inject
  protected ImportAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ReactorStatusService paramReactorStatusService, WebAuthnProviderService paramWebAuthnProviderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramReactorStatusService);
    this.webAuthnProviderService = paramWebAuthnProviderService;
  }
  
  public String post() {
    try {
      this.webAuthnProviderService.createBulk(getTenant(), this.request.credentials);
    } catch (Exception exception) {
      this.frontEndSupport.addGeneralError("[WebAuthnCredentialImportRequestFailed]", new Object[0]);
      EventLogHelper.create(new EventLog(EventLogType.Error, "WebAuthn Credential Import request failed. This is likely a database FK violation.", exception));
      return "input";
    } 
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.request.credentials == null || this.request.credentials.isEmpty()) {
      this.frontEndSupport.addFieldError("credentials", "[missing]credentials", new Object[0]);
      return;
    } 
    Errors errors = this.webAuthnProviderService.validateBulkImport(getTenant(), this.request.validateDbConstraints, this.request.credentials);
    this.frontEndSupport.transfer(errors);
  }
}
