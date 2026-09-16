package io.fusionauth.api.service.tenant;

import io.fusionauth.domain.RateLimitedRequestConfiguration;
import io.fusionauth.domain.RateLimitedRequestType;
import io.fusionauth.domain.TenantRateLimitConfiguration;

public class RateLimitHelper {
  public static RateLimitedRequestConfiguration getConfiguration(TenantRateLimitConfiguration paramTenantRateLimitConfiguration, RateLimitedRequestType paramRateLimitedRequestType) {
    switch (paramRateLimitedRequestType) {
      default:
        throw new MatchException(null, null);
      case FailedLogin:
      
      case ForgotPassword:
      
      case SendEmailVerification:
      
      case SendPasswordless:
      
      case SendPhonePasswordless:
      
      case SendPhoneVerification:
      
      case SendRegistrationVerification:
      
      case SendTwoFactor:
        break;
    } 
    return 






      
      paramTenantRateLimitConfiguration.sendTwoFactor;
  }
}
