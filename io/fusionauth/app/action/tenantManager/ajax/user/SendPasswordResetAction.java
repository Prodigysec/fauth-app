package io.fusionauth.app.action.tenantManager.ajax.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.action.ajax.user.IdentitySelector;
import io.fusionauth.app.action.tenantManager.BaseTenantManagerAction;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.user.ForgotPasswordRequest;
import io.fusionauth.domain.api.user.ForgotPasswordResponse;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.annotation.ConstraintOverride;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Action(value = "{userId}", requiresAuthentication = true, scheme = {"tenant-manager"})
public class SendPasswordResetAction extends BaseTenantManagerAction implements IdentitySelector {
  public String loginId;
  
  public UUID userId;
  
  @Inject
  public SendPasswordResetAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  @ConstraintOverride({"admin"})
  public String get() {
    return "input";
  }
  
  @ConstraintOverride({"admin"})
  public String post() {
    ClientResponse<UserResponse, Errors> clientResponse = this.client.retrieveUser(this.userId);
    if (!clientResponse.wasSuccessful()) {
      this.frontEndSupport.addGeneralError("[notFound]userId", new Object[0]);
      return "error";
    } 
    User user = ((UserResponse)clientResponse.successResponse).user;
    String str = (this.loginId != null && !this.loginId.isBlank()) ? this.loginId : user.email;
    UserIdentity userIdentity = resolveUserIdentity(user.identities, str);
    Errors errors = validateSelectedIdentity(str, userIdentity);
    if (errors != null && errors.size() > 0) {
      this.frontEndSupport.transfer(errors);
      return "error";
    } 
    boolean bool = (userIdentity != null && userIdentity.type.is(IdentityType.phoneNumber)) ? ((this.tenant.phoneConfiguration.forgotPasswordTemplateId != null) ? true : false) : ((this.tenant.emailConfiguration.forgotPasswordEmailTemplateId != null) ? true : false);
    if (userIdentity != null && !bool) {
      this.frontEndSupport.addGeneralError(userIdentity.type.is(IdentityType.email) ? 
          "[disabled]sendResetPasswordEmail" : 
          "[disabled]sendResetPasswordPhone", new Object[0]);
      return "error";
    } 
    ForgotPasswordRequest forgotPasswordRequest = (new ForgotPasswordRequest(this.frontEndSupport.buildEventInfo(null), null, userIdentity.value)).with(paramForgotPasswordRequest -> paramForgotPasswordRequest.loginIdTypes = List.of(paramUserIdentity.type.name)).with(paramForgotPasswordRequest -> paramForgotPasswordRequest.sendForgotPasswordMessage = Boolean.valueOf(true));
    ClientResponse<ForgotPasswordResponse, Errors> clientResponse1 = this.client.forgotPassword(forgotPasswordRequest);
    if (clientResponse1.wasSuccessful()) {
      writeAuditLog("Sent password reset message to user with Id [" + String.valueOf(user.id) + "], name [" + user.getName() + "] and loginId [" + forgotPasswordRequest.loginId + "]");
      this.frontEndSupport.addGeneralInfo(MessageScope.FLASH, "[PasswordResetSent]", new Object[0]);
      return "success";
    } 
    this.frontEndSupport.transfer((Errors)clientResponse1.errorResponse);
    return "error";
  }
}
