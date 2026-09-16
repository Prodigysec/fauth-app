package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.jwt.FusionAuthJWTDecoder;
import io.fusionauth.api.service.jwt.JWTClaimValidator;
import io.fusionauth.api.service.jwt.ValidatedJWTResult;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.api.util.ClaimTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.domain.JWT;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.primeframework.mvc.security.JWTConstraintsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusionAuthScopedJWTConstraintValidator implements JWTConstraintsValidator {
  private static final Logger logger = LoggerFactory.getLogger(FusionAuthScopedJWTConstraintValidator.class);
  
  private final ApplicationCache applicationCache;
  
  private final JWTClaimValidator jwtClaimValidator;
  
  private final KeyCache keyCache;
  
  private final HTTPRequest request;
  
  private final TenantCache tenantCache;
  
  @Inject
  public FusionAuthScopedJWTConstraintValidator(ApplicationCache paramApplicationCache, JWTClaimValidator paramJWTClaimValidator, KeyCache paramKeyCache, HTTPRequest paramHTTPRequest, TenantCache paramTenantCache) {
    this.applicationCache = paramApplicationCache;
    this.jwtClaimValidator = paramJWTClaimValidator;
    this.keyCache = paramKeyCache;
    this.request = paramHTTPRequest;
    this.tenantCache = paramTenantCache;
  }
  
  public boolean validate(JWT paramJWT, String[] paramArrayOfString) {
    if (paramJWT == null || paramArrayOfString == null)
      return false; 
    ValidatedJWTResult validatedJWTResult = FusionAuthJWTDecoder.validateConstraints(paramJWT, FusionAuthJWTDecoder.JWTConstraints.UserAccessToken);
    if (!validatedJWTResult.valid) {
      logger.debug(validatedJWTResult.getMessage());
      return false;
    } 
    String str = (paramJWT.header == null) ? null : paramJWT.header.getString("kid");
    if (str == null) {
      logger.debug("Scoped JWT validation failed. No [kid] header present.");
      return false;
    } 
    Key key = this.keyCache.getVerifierKeyByKid(str);
    if (key == null) {
      logger.debug("Scoped JWT validation failed. No key found for [kid] [{}].", str);
      return false;
    } 
    Optional<UUID> optional1 = ActionTools.resolveTenantIdFromHeader(this.request);
    Optional<UUID> optional2 = ClaimTools.getTenantIdFromTidClaim(paramJWT);
    Optional<UUID> optional3 = optional1.isPresent() ? optional1 : optional2;
    Objects.requireNonNull(this.tenantCache);
    Tenant tenant = optional3.<Tenant>map(this.tenantCache::get).orElse(null);
    if (tenant == null) {
      logger.debug("Scoped JWT validation failed. No tenant resolved for request tenant [{}] and token [tid] [{}].", optional1, optional2);
      return false;
    } 
    Optional<UUID> optional4 = ClaimTools.getAppIdFromAudClaim(paramJWT);
    Optional<?> optional = optional4.map(paramUUID -> this.applicationCache.get(paramTenant.id, paramUUID));
    if (optional.isPresent()) {
      if (!(this.jwtClaimValidator.validateForApplication(paramJWT, tenant, (Application)optional.get()) instanceof JWTClaimValidator.ClaimValidationResult.Valid)) {
        logger.debug("Scoped JWT validation failed. Token claims do not match the resolved tenant/application [{}]/[{}].", tenant.id, ((Application)optional.get()).id);
        return false;
      } 
    } else if (!(this.jwtClaimValidator.validateForTenant(paramJWT, tenant) instanceof JWTClaimValidator.ClaimValidationResult.Valid)) {
      logger.debug("Scoped JWT validation failed. Token claims do not match the resolved tenant [{}].", tenant.id);
      return false;
    } 
    if (paramArrayOfString.length > 0) {
      String str1 = paramJWT.getString("applicationId");
      if (!Application.FUSIONAUTH_APP_ID.toString().equals(str1)) {
        logger.debug("Scoped JWT validation failed. [applicationId] claim is not for the FusionAuth admin application.");
        return false;
      } 
      List list = paramJWT.getList("roles");
      Objects.requireNonNull(list);
      if (list == null || Arrays.<String>stream(paramArrayOfString).noneMatch(list::contains)) {
        logger.debug("Scoped JWT validation failed. Required role not present in [roles] claim.");
        return false;
      } 
    } 
    return true;
  }
}
