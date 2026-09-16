package io.fusionauth.api.service.mfa;

import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import javax.annotation.Nonnull;

public interface MFALifecycleService {
  void onChallenge(Application paramApplication, EventInfo paramEventInfo, ExternalIdentifier paramExternalIdentifier, @Nonnull Tenant paramTenant, User paramUser);
  
  void onFailedAttempt(Application paramApplication, EventInfo paramEventInfo, ExternalIdentifier paramExternalIdentifier, @Nonnull Tenant paramTenant, User paramUser);
  
  void onSuccess(Application paramApplication, EventInfo paramEventInfo, ExternalIdentifier paramExternalIdentifier, @Nonnull Tenant paramTenant, @Nonnull AuthenticationService.TwoFactorValidationResult paramTwoFactorValidationResult, User paramUser);
}
