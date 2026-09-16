package io.fusionauth.app.action.ajax.user;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.api.identity.verify.VerifyRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;

@Action(value = "{userId}", requiresAuthentication = true, constraints = {"admin", "user_manager"})
public class VerifyIdentityAction extends BaseSelectIdentityAJAXAction {
  @Inject
  public VerifyIdentityAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.verifyIdentity((new VerifyRequest()).with(()).with(())));
    writeAuditLog("Administratively verified identity [" + this.identity.value + "] for user with Id [" + String.valueOf(this.user.id) + "], name [" + this.user.getName() + "]");
    this.frontEndSupport.addGeneralInfo(MessageScope.FLASH, "[VerifyIdentitySuccess]", new Object[0]);
    return "success";
  }
  
  @PreValidationMethod
  public void preValidation() {
    loadUserPrepareDelegate();
  }
  
  @FormPrepareMethod
  public void prepare() {
    this.identities = prepareFormIdentities(this.user.identities, UserIdentity::verificationRequired);
  }
  
  @ValidationMethod
  public void validate() {
    this.identity = resolveUserIdentity(this.user.identities, this.loginId);
    this.frontEndSupport.transfer(validateSelectedIdentity(this.loginId, this.identity));
  }
}
