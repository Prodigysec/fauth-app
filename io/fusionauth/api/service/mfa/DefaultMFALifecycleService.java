package io.fusionauth.api.service.mfa;

import com.google.inject.Inject;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.MFAMetricsMapper;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.event.UserTwoFactorChallengeEvent;
import io.fusionauth.domain.event.UserTwoFactorFailedAttemptEvent;
import io.fusionauth.domain.event.UserTwoFactorSuccessEvent;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;

public class DefaultMFALifecycleService implements MFALifecycleService {
  private final MFAMetricsMapper metricsMapper;
  
  @Inject
  public DefaultMFALifecycleService(MFAMetricsMapper paramMFAMetricsMapper) {
    this.metricsMapper = paramMFAMetricsMapper;
  }
  
  public void onChallenge(Application paramApplication, EventInfo paramEventInfo, ExternalIdentifier paramExternalIdentifier, @Nonnull Tenant paramTenant, User paramUser) {
    this.metricsMapper.incrementChallengeCount(paramTenant.id);
    sendTwoFactorChallengeWebhook(paramApplication, paramEventInfo, resolveClientRisk(paramExternalIdentifier), paramTenant, paramUser);
  }
  
  public void onFailedAttempt(Application paramApplication, EventInfo paramEventInfo, ExternalIdentifier paramExternalIdentifier, @Nonnull Tenant paramTenant, User paramUser) {
    this.metricsMapper.incrementFailedAttemptCount(paramTenant.id);
    String str1 = paramExternalIdentifier.getAttribute("twoFactorMessageType");
    String str2 = resolveFailedMethod(paramExternalIdentifier, paramUser);
    String str3 = resolveClientRisk(paramExternalIdentifier);
    sendTwoFactorFailureWebhook(paramApplication, paramEventInfo, str1, str2, str3, paramTenant, paramUser);
  }
  
  public void onSuccess(Application paramApplication, EventInfo paramEventInfo, ExternalIdentifier paramExternalIdentifier, @Nonnull Tenant paramTenant, @Nonnull AuthenticationService.TwoFactorValidationResult paramTwoFactorValidationResult, User paramUser) {
    this.metricsMapper.incrementSuccessCount(paramTenant.id);
    String str1 = paramExternalIdentifier.getAttribute("twoFactorMessageType");
    String str2 = resolveSuccessMethod(paramTwoFactorValidationResult, paramUser);
    String str3 = resolveClientRisk(paramExternalIdentifier);
    sendTwoFactorSuccessWebhook(paramApplication, paramEventInfo, str1, str2, str3, paramTenant, paramUser);
  }
  
  private UUID applicationIdOrNull(Application paramApplication) {
    return (paramApplication != null) ? paramApplication.id : null;
  }
  
  private String resolveClientRisk(ExternalIdentifier paramExternalIdentifier) {
    if (paramExternalIdentifier == null)
      return "NOT_COMPUTED"; 
    String str = paramExternalIdentifier.getAttribute("riskLevel");
    return StringUtils.isBlank(str) ? "NOT_COMPUTED" : str;
  }
  
  private String resolveFailedMethod(ExternalIdentifier paramExternalIdentifier, User paramUser) {
    String str1 = "";
    String str2 = paramExternalIdentifier.getAttribute("methodId");
    if (str2 == null) {
      boolean bool = paramUser.twoFactor.methods.stream().anyMatch(paramTwoFactorMethod -> "authenticator".equals(paramTwoFactorMethod.method));
      if (bool)
        str1 = "authenticator"; 
    } else {
      TwoFactorMethod twoFactorMethod = paramUser.twoFactor.getMethodById(str2);
      str1 = (twoFactorMethod != null) ? twoFactorMethod.method : "";
    } 
    return str1;
  }
  
  private String resolveSuccessMethod(AuthenticationService.TwoFactorValidationResult paramTwoFactorValidationResult, User paramUser) {
    if (paramTwoFactorValidationResult instanceof AuthenticationService.TwoFactorValidationResult.RecoveryCodeSuccess)
      return "recoveryCode"; 
    TwoFactorMethod twoFactorMethod = paramUser.twoFactor.getLastUsedMethod();
    return (twoFactorMethod != null) ? twoFactorMethod.method : null;
  }
  
  private void sendTwoFactorChallengeWebhook(Application paramApplication, EventInfo paramEventInfo, String paramString, Tenant paramTenant, User paramUser) {
    EventHelper.send(paramTenant, paramApplication, new UserTwoFactorChallengeEvent(paramEventInfo, paramString, 
          
          applicationIdOrNull(paramApplication), paramUser));
  }
  
  private void sendTwoFactorFailureWebhook(Application paramApplication, EventInfo paramEventInfo, String paramString1, String paramString2, String paramString3, Tenant paramTenant, User paramUser) {
    EventHelper.send(paramTenant, paramApplication, new UserTwoFactorFailedAttemptEvent(paramEventInfo, paramString3, paramString1, paramString2, 


          
          applicationIdOrNull(paramApplication), paramUser));
  }
  
  private void sendTwoFactorSuccessWebhook(Application paramApplication, EventInfo paramEventInfo, String paramString1, String paramString2, String paramString3, Tenant paramTenant, User paramUser) {
    EventHelper.send(paramTenant, paramApplication, new UserTwoFactorSuccessEvent(paramEventInfo, paramString3, paramString1, paramString2, 


          
          applicationIdOrNull(paramApplication), paramUser));
  }
}
