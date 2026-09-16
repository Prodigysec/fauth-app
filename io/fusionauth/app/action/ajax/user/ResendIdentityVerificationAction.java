package io.fusionauth.app.action.ajax.user;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.TenantResponse;
import io.fusionauth.domain.api.identity.verify.VerifySendRequest;
import io.fusionauth.domain.api.identity.verify.VerifyStartRequest;
import io.fusionauth.domain.api.identity.verify.VerifyStartResponse;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;

@Action(value = "{userId}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class ResendIdentityVerificationAction extends BaseSelectIdentityAJAXAction {
  public UUID applicationId;
  
  public List<Application> applications;
  
  private Tenant tenant;
  
  @Inject
  public ResendIdentityVerificationAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    VerifyStartResponse verifyStartResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.startVerifyIdentity((new VerifyStartRequest()).with(()).with(()).with(())));
    AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    this.delegate.execute(paramFusionAuthClient -> {
          ClientResponse<Void, Errors> clientResponse = paramFusionAuthClient.sendVerifyIdentity(new VerifySendRequest(paramVerifyStartResponse.verificationId));
          if (!clientResponse.wasSuccessful() && ((Errors)clientResponse.errorResponse).containsError("[MessengerError]")) {
            this.frontEndSupport.addGeneralError(MessageScope.FLASH, "[MessengerError]", new Object[0]);
            paramAtomicBoolean.set(true);
            return new ClientResponse();
          } 
          return clientResponse;
        });
    if (!atomicBoolean.get()) {
      writeAuditLog("Resent identity verification to user with Id [" + String.valueOf(this.user.id) + "], name [" + this.user.getName() + "] to [" + this.identity.value + "]");
      if (IdentityType.phoneNumber.is(this.identity.type)) {
        this.frontEndSupport.addGeneralInfo(MessageScope.FLASH, "[SendSMSSuccess]", new Object[0]);
      } else {
        this.frontEndSupport.addGeneralInfo(MessageScope.FLASH, "[SendEmailSuccess]", new Object[0]);
      } 
    } 
    return "success";
  }
  
  @PostValidationMethod
  public void postValidation() {
    this.tenant = ((TenantResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTenant(this.user.tenantId))).tenant;
  }
  
  @PreValidationMethod
  public void preValidation() {
    loadUserPrepareDelegate();
  }
  
  @FormPrepareMethod
  public void prepare() {
    this.identities = prepareFormIdentities(this.user.identities, UserIdentity::verificationRequired);
    if (!this.tenant.emailConfiguration.verifyEmail)
      this.identities.removeIf(paramUserIdentity -> paramUserIdentity.type.is(IdentityType.email)); 
    if (!this.tenant.phoneConfiguration.verifyPhoneNumber)
      this.identities.removeIf(paramUserIdentity -> paramUserIdentity.type.is(IdentityType.phoneNumber)); 
    boolean bool1 = this.identities.stream().anyMatch(paramUserIdentity -> paramUserIdentity.type.is(IdentityType.email));
    boolean bool2 = this.identities.stream().anyMatch(paramUserIdentity -> paramUserIdentity.type.is(IdentityType.phoneNumber));
    this






      
      .applications = ((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveApplications)).applications.stream().filter(paramApplication -> ((paramBoolean1 && paramApplication.emailConfiguration.emailVerificationEmailTemplateId != null) || (paramBoolean2 && paramApplication.phoneConfiguration.verificationTemplateId != null))).sorted(Comparator.comparing(paramApplication -> paramApplication.name)).toList();
  }
  
  @ValidationMethod
  public void validate() {
    this.identity = resolveUserIdentity(this.user.identities, this.loginId);
    this.frontEndSupport.transfer(validateSelectedIdentity(this.loginId, this.identity));
  }
}
