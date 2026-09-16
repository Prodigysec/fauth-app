package io.fusionauth.app.action.ajax.user.webauthn;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.webauthn.WebAuthnFrontendService;
import io.fusionauth.domain.api.WebAuthnRegisterCompleteRequest;
import io.fusionauth.domain.api.WebAuthnRegisterCompleteResponse;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.security.UnauthorizedException;
import org.primeframework.mvc.validation.ValidationMethod;

@Status(code = "success", status = 200)
@Action(requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager", "user_support_viewer"})
public class AddAction extends BaseAJAXAction {
  private final WebAuthnFrontendService webAuthnFrontendService;
  
  public String displayName;
  
  public UUID userId;
  
  public String webAuthnRegisterRequest;
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport, WebAuthnFrontendService paramWebAuthnFrontendService) {
    super(paramFrontEndSupport);
    this.webAuthnFrontendService = paramWebAuthnFrontendService;
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    if (this.userId == null || !this.userId.equals(this.codeCurrentUser.id))
      throw new UnauthorizedException(); 
    WebAuthnRegisterCompleteRequest webAuthnRegisterCompleteRequest = this.webAuthnFrontendService.unmarshalWebAuthnRegistrationRequest(this.webAuthnRegisterRequest, this.codeCurrentUser.id);
    if (webAuthnRegisterCompleteRequest == null) {
      this.frontEndSupport.addGeneralError(MessageScope.FLASH, "[InvalidWebAuthnBrowserResponse]", new Object[0]);
      return "success";
    } 
    ClientResponse<WebAuthnRegisterCompleteResponse, Errors> clientResponse = this.client.completeWebAuthnRegistration(webAuthnRegisterCompleteRequest);
    if (!clientResponse.wasSuccessful())
      this.frontEndSupport.addGeneralError(MessageScope.FLASH, "[WebAuthnFailed]", new Object[0]); 
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    Objects.requireNonNull(this.frontEndSupport);
    (new Validator()).notBlank(this.displayName, "displayName", new Object[0]).done(this.frontEndSupport::transfer);
  }
}
