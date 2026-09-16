package io.fusionauth.app.action.oauth2.ajax.webauthn;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.SecurityTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.oauth2.BaseOAuthAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.api.WebAuthnCredentialResponse;
import io.fusionauth.domain.api.WebAuthnStartRequest;
import io.fusionauth.domain.api.WebAuthnStartResponse;
import io.fusionauth.domain.webauthn.PublicKeyCredentialDescriptor;
import io.fusionauth.domain.webauthn.PublicKeyCredentialRequestOptions;
import io.fusionauth.domain.webauthn.UserVerificationRequirement;
import io.fusionauth.domain.webauthn.WebAuthnWorkflow;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class StartLoginAction extends BaseOAuthAJAXAction {
  public UUID credentialId;
  
  @JSONResponse
  public WebAuthnStartResponse response;
  
  public WebAuthnWorkflow workflow;
  
  private UUID userId;
  
  @Inject
  protected StartLoginAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    mapWebAuthnFieldErrorsToGeneralErrors();
  }
  
  public String post() {
    ClientResponse<WebAuthnStartResponse, Errors> clientResponse = this.client.startWebAuthnLogin((new WebAuthnStartRequest(this.codeApplication.id, this.userId, this.loginId, this.credentialId, null, this.workflow))
        
        .with(paramWebAuthnStartRequest -> paramWebAuthnStartRequest.loginIdTypes = StandardLoginIdTypes));
    if (clientResponse.wasSuccessful()) {
      this.response = (WebAuthnStartResponse)clientResponse.successResponse;
    } else {
      if (clientResponse.status == 404) {
        buildDummyCredential();
        return "render-json";
      } 
      if (clientResponse.errorResponse != null) {
        if (((Errors)clientResponse.errorResponse).fieldErrors.containsKey("workflow"))
          return "disabled"; 
        if (((Errors)clientResponse.errorResponse).fieldErrors.containsKey("loginId") || ((Errors)clientResponse.errorResponse).fieldErrors.containsKey("userId")) {
          buildDummyCredential();
          return "render-json";
        } 
        transferErrors((Errors)clientResponse.errorResponse);
        throw new ErrorException("input", false);
      } 
    } 
    return "render-json";
  }
  
  @PostParameterMethod
  public void postParameter() {
    if (this.credentialId != null) {
      ClientResponse<WebAuthnCredentialResponse, Errors> clientResponse = this.client.retrieveWebAuthnCredential(this.credentialId);
      if (clientResponse.wasSuccessful())
        this.userId = ((WebAuthnCredentialResponse)clientResponse.successResponse).credential.userId; 
    } 
  }
  
  @ValidationMethod
  public void validate() {
    StartLoginAction startLoginAction = this;
    (new Validator()).notBlank(this.client_id, "client_id", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.validObjectWithCode(this.codeApplication, "client_id", "[invalid]client_id", new Object[0])).ifTrue((this.credentialId == null), paramValidator -> paramValidator.notBlank(this.loginId, "loginId", new Object[0])).ifTrue((this.credentialId != null), paramValidator -> paramValidator.ensure((this.userId != null), "credentialId", "[invalid]", new Object[0])).notBlank(this.workflow, "workflow", new Object[] { this.workflow }).done(paramErrors -> paramStartLoginAction.transferErrors(paramErrors));
  }
  
  private void buildDummyCredential() {
    this










      
      .response = new WebAuthnStartResponse((new PublicKeyCredentialRequestOptions()).with(paramPublicKeyCredentialRequestOptions -> paramPublicKeyCredentialRequestOptions.allowCredentials = List.of((new PublicKeyCredentialDescriptor()).with(()).with(()))).with(paramPublicKeyCredentialRequestOptions -> paramPublicKeyCredentialRequestOptions.challenge = SecurityTools.secureRandom(32)).with(paramPublicKeyCredentialRequestOptions -> paramPublicKeyCredentialRequestOptions.relyingPartyId = this.codeTenant.webAuthnConfiguration.relyingPartyId).with(paramPublicKeyCredentialRequestOptions -> paramPublicKeyCredentialRequestOptions.timeout = this.codeTenant.externalIdentifierConfiguration.webAuthnRegistrationChallengeTimeToLiveInSeconds * 1000L).with(paramPublicKeyCredentialRequestOptions -> paramPublicKeyCredentialRequestOptions.userVerification = UserVerificationRequirement.required));
  }
}
