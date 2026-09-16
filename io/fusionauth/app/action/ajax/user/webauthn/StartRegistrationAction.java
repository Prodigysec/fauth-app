package io.fusionauth.app.action.ajax.user.webauthn;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.WebAuthnRegisterStartRequest;
import io.fusionauth.domain.api.WebAuthnRegisterStartResponse;
import io.fusionauth.domain.webauthn.WebAuthnWorkflow;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.security.UnauthorizedException;

@JSON(code = "input", status = 400)
@Action(requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager", "user_support_viewer"})
public class StartRegistrationAction extends BaseAJAXAction {
  public String displayName;
  
  @JSONResponse
  public WebAuthnRegisterStartResponse response;
  
  public String userAgent;
  
  public UUID userId;
  
  @Inject
  protected StartRegistrationAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String post() {
    if (this.userId == null || !this.userId.equals(this.codeCurrentUser.id))
      throw new UnauthorizedException(); 
    this.response = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.startWebAuthnRegistration((new WebAuthnRegisterStartRequest()).with(()).with(()).with(()).with(())));
    return "render-json";
  }
}
