package io.fusionauth.app.action;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import com.inversoft.util.StringTools;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.api.identity.verify.VerifyCompleteRequest;
import io.fusionauth.domain.api.identity.verify.VerifyCompleteResponse;
import io.fusionauth.domain.api.identity.verify.VerifySendRequest;
import io.fusionauth.domain.api.identity.verify.VerifyStartRequest;
import io.fusionauth.domain.api.identity.verify.VerifyStartResponse;
import io.fusionauth.domain.api.user.VerifyRegistrationResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.parameter.annotation.UnknownParameters;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.validation.ValidationMethod;

@List({@Redirect(code = "sent", uri = "${redirectToSent}"), @Redirect(code = "complete", uri = "${redirectToComplete}"), @Redirect(code = "register", uri = "${redirectToRegister}")})
public abstract class BaseVerifyAction extends BaseThemedAction {
  public String captcha_token;
  
  public String redirectToComplete;
  
  public String redirectToRegister;
  
  public String redirectToSent;
  
  public boolean showCaptcha;
  
  @UnknownParameters
  public Map<String, Object> unknownParameters = new HashMap<>();
  
  public String verificationId;
  
  protected BaseVerifyAction(FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
  }
  
  @PreRenderMethod({ThemedForward.class})
  public void prePageRenderSetup() {
    this.showCaptcha = (this.showCaptcha || (this.verificationId == null && showCaptchaOnInitialPageRender(null)));
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.verificationId == null)
      addGeneralError("[MissingVerificationId]", new Object[0]); 
  }
  
  protected Map<String, Object> captureState() {
    if (this.client_id == null)
      return null; 
    return CollectionTools.mapNV(new Object[] { "client_id", this.client_id, "tenantId", this.codeTenant.id });
  }
  
  protected String handleResendResponse(ClientResponse<?, Errors> paramClientResponse, String paramString, IdentityType paramIdentityType) {
    if (paramClientResponse.wasSuccessful() || paramClientResponse.status == 404) {
      String str;
      if (paramClientResponse.wasSuccessful()) {
        Object object = paramClientResponse.successResponse;
        if (object instanceof VerifyStartResponse) {
          VerifyStartResponse verifyStartResponse = (VerifyStartResponse)object;
          if (verifyStartResponse.verificationId != null)
            allowConfirmationBypass(); 
        } else {
          object = paramClientResponse.successResponse;
          if (object instanceof VerifyRegistrationResponse) {
            VerifyRegistrationResponse verifyRegistrationResponse = (VerifyRegistrationResponse)object;
            if (verifyRegistrationResponse.verificationId != null)
              allowConfirmationBypass(); 
          } 
        } 
      } 
      if (paramIdentityType.is(IdentityType.email)) {
        str = "email";
      } else if (paramIdentityType.is(IdentityType.phoneNumber)) {
        str = "phoneNumber";
      } else {
        throw new IllegalArgumentException("Unsupported identity type: " + String.valueOf(paramIdentityType));
      } 
      this


        
        .redirectToSent = QueryStringBuilder.builder(getBaseSentURI()).with("client_id", this.client_id).with(str, paramString).with("tenantId", this.codeTenant.id).build();
      return "sent";
    } 
    if (paramClientResponse.status == 400) {
      if (((Errors)paramClientResponse.errorResponse).containsError("[disabled]loginIdType")) {
        if (paramIdentityType.is(IdentityType.email)) {
          addGeneralError("[EmailVerificationDisabled]", new Object[0]);
        } else if (paramIdentityType.is(IdentityType.phoneNumber)) {
          addGeneralError("[PhoneVerificationDisabled]", new Object[0]);
        } else {
          throw new IllegalArgumentException("Unsupported identity type: " + String.valueOf(paramIdentityType));
        } 
      } else {
        transferErrors((Errors)paramClientResponse.errorResponse);
      } 
    } else if (paramClientResponse.status == 403) {
      addGeneralError("[EmailVerificationDisabled]", new Object[0]);
    } else if (((Errors)paramClientResponse.errorResponse).containsError("[MessengerError]")) {
      addGeneralError("[MessengerError]", new Object[0]);
    } else {
      addGeneralError("[APIError]", new Object[0]);
    } 
    return "input";
  }
  
  protected String handleVerifyResponse(String paramString, ClientResponse<?, Errors> paramClientResponse) {
    if (paramClientResponse.wasSuccessful()) {
      Object object1 = this.client_id;
      UUID uUID = this.codeTenant.id;
      Object object2 = paramClientResponse.successResponse;
      if (object2 instanceof VerifyCompleteResponse) {
        VerifyCompleteResponse verifyCompleteResponse = (VerifyCompleteResponse)object2;
        if (verifyCompleteResponse.state != null) {
          object2 = safeGet(verifyCompleteResponse.state, "client_id");
          if (object2 != null)
            object1 = object2; 
          String str1 = safeGet(verifyCompleteResponse.state, "tenantId");
          if (str1 != null)
            uUID = StringTools.parseUUID(str1); 
          String str2 = (String)verifyCompleteResponse.state.get("redirectDestination");
          if (str2 != null) {
            this
              
              .redirectToRegister = QueryStringBuilder.builder(str2).with("externalId", paramString).build();
            return "register";
          } 
        } 
      } 
      this

        
        .redirectToComplete = QueryStringBuilder.builder(getBaseCompleteURI()).with("client_id", object1).with("tenantId", uUID).build();
      removeConfirmationBypass();
      return "complete";
    } 
    if (paramClientResponse.status == 400) {
      transferErrors((Errors)paramClientResponse.errorResponse);
    } else if (paramClientResponse.status == 404) {
      addGeneralError("[InvalidVerificationId]", new Object[0]);
    } else {
      addGeneralError("[APIError]", new Object[0]);
      return "error";
    } 
    return "input";
  }
  
  protected String resendIdentityVerificationAndRedirect(String paramString, IdentityType paramIdentityType) {
    UUID uUID = (this.codeApplication != null) ? this.codeApplication.id : null;
    ClientResponse<VerifyStartResponse, Errors> clientResponse = this.client.startVerifyIdentity((new VerifyStartRequest())
        .with(paramVerifyStartRequest -> paramVerifyStartRequest.applicationId = paramUUID)
        .with(paramVerifyStartRequest -> paramVerifyStartRequest.loginId = paramString)
        .with(paramVerifyStartRequest -> paramVerifyStartRequest.loginIdType = paramIdentityType.name)
        .with(paramVerifyStartRequest -> paramVerifyStartRequest.state = captureState()));
    if (clientResponse.wasSuccessful()) {
      ClientResponse<Void, Errors> clientResponse1 = this.client.sendVerifyIdentity(new VerifySendRequest(((VerifyStartResponse)clientResponse.successResponse).verificationId));
      if (!clientResponse1.wasSuccessful())
        return handleResendResponse(clientResponse1, paramString, paramIdentityType); 
    } 
    return handleResendResponse(clientResponse, paramString, paramIdentityType);
  }
  
  protected String safeGet(Map<String, Object> paramMap, String paramString) {
    Object object = paramMap.get(paramString);
    return (object == null) ? null : object.toString();
  }
  
  protected String verifyIdentity() {
    VerifyCompleteRequest verifyCompleteRequest = (new VerifyCompleteRequest()).with(paramVerifyCompleteRequest -> paramVerifyCompleteRequest.eventInfo = this.frontEndSupport.buildEventInfo(null)).with(paramVerifyCompleteRequest -> paramVerifyCompleteRequest.verificationId = this.verificationId);
    ClientResponse<VerifyCompleteResponse, Errors> clientResponse = this.client.completeVerifyIdentity(verifyCompleteRequest);
    return handleVerifyResponse(this.verificationId, clientResponse);
  }
  
  protected abstract String getBaseCompleteURI();
  
  protected abstract String getBaseSentURI();
}
