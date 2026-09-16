package io.fusionauth.app.action.ajax.user;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.TenantResponse;
import io.fusionauth.domain.api.user.ForgotPasswordRequest;
import io.fusionauth.domain.api.user.ForgotPasswordResponse;
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
public class SendPasswordResetAction extends BaseSelectIdentityAJAXAction {
  public UUID applicationId;
  
  @FTLVariable
  public List<Application> applications;
  
  private Tenant tenant;
  
  @Inject
  public SendPasswordResetAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    ForgotPasswordRequest forgotPasswordRequest = (new ForgotPasswordRequest(this.frontEndSupport.buildEventInfo(null), this.applicationId, this.identity.value)).with(paramForgotPasswordRequest -> paramForgotPasswordRequest.loginIdTypes = List.of(this.identity.type.name)).with(paramForgotPasswordRequest -> paramForgotPasswordRequest.sendForgotPasswordMessage = Boolean.valueOf(true));
    AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    this.delegate.execute(paramFusionAuthClient -> {
          ClientResponse<ForgotPasswordResponse, Errors> clientResponse = paramFusionAuthClient.forgotPassword(paramForgotPasswordRequest);
          if (!clientResponse.wasSuccessful() && ((Errors)clientResponse.errorResponse).containsError("[MessengerError]")) {
            this.frontEndSupport.addGeneralError(MessageScope.FLASH, "[MessengerError]", new Object[0]);
            paramAtomicBoolean.set(true);
            return new ClientResponse();
          } 
          return clientResponse;
        });
    if (!atomicBoolean.get()) {
      writeAuditLog("Sent password reset message to user with Id [" + String.valueOf(this.user.id) + "], name [" + this.user.getName() + "] and loginId [" + forgotPasswordRequest.loginId + "]");
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
    this.identities = prepareFormIdentities(this.user.identities, paramUserIdentity -> (paramUserIdentity.type.isNot(IdentityType.username) && paramUserIdentity.primary));
    if (this.tenant.emailConfiguration.forgotPasswordEmailTemplateId == null)
      this.identities.removeIf(paramUserIdentity -> paramUserIdentity.type.is(IdentityType.email)); 
    if (this.tenant.phoneConfiguration.messengerId == null || this.tenant.phoneConfiguration.forgotPasswordTemplateId == null)
      this.identities.removeIf(paramUserIdentity -> paramUserIdentity.type.is(IdentityType.phoneNumber)); 
    boolean bool1 = this.identities.stream().anyMatch(paramUserIdentity -> paramUserIdentity.type.is(IdentityType.email));
    boolean bool2 = this.identities.stream().anyMatch(paramUserIdentity -> paramUserIdentity.type.is(IdentityType.phoneNumber));
    this






      
      .applications = ((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveApplications)).applications.stream().filter(paramApplication -> ((paramBoolean1 && paramApplication.emailConfiguration.forgotPasswordEmailTemplateId != null) || (paramBoolean2 && paramApplication.phoneConfiguration.forgotPasswordTemplateId != null))).sorted(Comparator.comparing(paramApplication -> paramApplication.name)).toList();
  }
  
  @ValidationMethod
  public void validate() {
    this.identity = resolveUserIdentity(this.user.identities, this.loginId);
    this.frontEndSupport.transfer(validateSelectedIdentity(this.loginId, this.identity));
  }
}
