package io.fusionauth.app.action.ajax.user.webauthn;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.WebAuthnCredential;
import io.fusionauth.domain.api.WebAuthnCredentialResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.security.UnauthorizedException;

@Action(value = "{id}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class DeleteAction extends BaseAJAXAction {
  @FTLVariable
  public WebAuthnCredential credential;
  
  public UUID id;
  
  public UUID userId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @FormPrepareMethod
  public void formPrepare() {
    this.credential = ((WebAuthnCredentialResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveWebAuthnCredential(this.id))).credential;
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    if (this.userId == null || (!hasRole(new String[] { "admin" }) && !this.userId.equals(this.codeCurrentUser.id)))
      throw new UnauthorizedException(); 
    ClientResponse<Void, Errors> clientResponse = this.client.deleteWebAuthnCredential(this.id);
    if (clientResponse.wasSuccessful()) {
      if (this.userId.equals(this.codeCurrentUser.id)) {
        writeAuditLog("A user with Id [" + String.valueOf(this.codeCurrentUser.id) + "] removed a WebAuthn credential from themselves. WebAuthn credential Id [" + String.valueOf(this.id) + "]");
      } else {
        writeAuditLog("A user with Id [" + String.valueOf(this.codeCurrentUser.id) + "] removed a WebAuthn credential for user with Id [" + String.valueOf(this.userId) + "]. WebAuthn credential Id [" + String.valueOf(this.id) + "]");
      } 
      return "success";
    } 
    return "input";
  }
}
