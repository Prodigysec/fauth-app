package io.fusionauth.api.service.security;

import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import java.util.UUID;

public interface ThreatDetectionService {
  CaptchaResult checkCaptchaChallenge(Tenant paramTenant, Application paramApplication, String paramString1, String paramString2, ReactorFeatureStatus paramReactorFeatureStatus);
  
  void handleBlockedIPAddress(Tenant paramTenant, Application paramApplication, String paramString);
  
  boolean isCaptchaEnabled(Tenant paramTenant, ReactorFeatureStatus paramReactorFeatureStatus);
  
  boolean isIPAddressBlocked(UUID paramUUID, String paramString);
}
