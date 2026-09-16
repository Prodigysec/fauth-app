package io.fusionauth.api.service.security;

import io.fusionauth.domain.RateLimitedRequestType;
import io.fusionauth.domain.Tenant;

public interface RateLimitService {
  void handleAndThrow(Tenant paramTenant, RateLimitedRequestType paramRateLimitedRequestType, String paramString);
  
  void handleDoNotThrow(Tenant paramTenant, RateLimitedRequestType paramRateLimitedRequestType, String paramString);
  
  boolean isRateLimited(Tenant paramTenant, RateLimitedRequestType paramRateLimitedRequestType, String paramString);
}
