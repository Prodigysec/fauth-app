package io.fusionauth.app.service.security;

import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.app.primeframework.PostAuthenticationStep;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.http.Cookie;
import java.time.ZonedDateTime;
import java.util.UUID;

public interface LoginIntentService {
  String buildLoginIntent(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, PostAuthenticationStep paramPostAuthenticationStep, SSOService.RememberDeviceState paramRememberDeviceState, String paramString);
  
  void deleteByUserId(UUID paramUUID);
  
  LoginIntent getLoginIntent(Tenant paramTenant, Application paramApplication, Cookie paramCookie);
  
  void updatePostAuthenticationStep(LoginIntent paramLoginIntent, PostAuthenticationStep paramPostAuthenticationStep, String paramString);
  
  public static class LoginIntent {
    public UUID applicationId;
    
    public String id;
    
    public ZonedDateTime insertInstant;
    
    public SSOService.RememberDeviceState rememberDeviceState;
    
    public PostAuthenticationStep step;
    
    public UUID tenantId;
    
    public User user;
    
    public String verificationId;
    
    public LoginIntent(UUID param1UUID1, UUID param1UUID2, User param1User, PostAuthenticationStep param1PostAuthenticationStep, SSOService.RememberDeviceState param1RememberDeviceState, String param1String) {
      this(param1UUID1, param1UUID2, null, param1User, null, param1PostAuthenticationStep, param1RememberDeviceState, param1String);
    }
    
    public LoginIntent(UUID param1UUID1, UUID param1UUID2, ZonedDateTime param1ZonedDateTime, User param1User, String param1String1, PostAuthenticationStep param1PostAuthenticationStep, SSOService.RememberDeviceState param1RememberDeviceState, String param1String2) {
      this.applicationId = param1UUID2;
      this.id = param1String1;
      this.insertInstant = param1ZonedDateTime;
      this.rememberDeviceState = param1RememberDeviceState;
      this.step = param1PostAuthenticationStep;
      this.tenantId = param1UUID1;
      this.user = param1User;
      this.verificationId = param1String2;
    }
    
    public ExternalIdentifier toExternalIdentifier() {
      ExternalIdentifier.ExternalIdData externalIdData = (new ExternalIdentifier.ExternalIdData()).setAttribute("postAuthenticationStep", this.step.name()).setAttribute("rememberDeviceState", this.rememberDeviceState.name()).setAttribute("verificationId", this.verificationId);
      return (new ExternalIdentifier()).with(param1ExternalIdentifier -> param1ExternalIdentifier.id = this.id)
        .with(param1ExternalIdentifier -> param1ExternalIdentifier.tenantId = this.tenantId)
        .with(param1ExternalIdentifier -> param1ExternalIdentifier.applicationId = this.applicationId)
        .with(param1ExternalIdentifier -> param1ExternalIdentifier.userId = this.user.id)
        .with(param1ExternalIdentifier -> param1ExternalIdentifier.type = ExternalIdentifier.ExternalIdType.LoginIntent)
        .with(param1ExternalIdentifier -> param1ExternalIdentifier.data = param1ExternalIdData);
    }
  }
}
