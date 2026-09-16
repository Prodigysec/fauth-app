package io.fusionauth.api.service.jwt;

import com.google.inject.Inject;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.jwt.claims.JWTType;
import io.fusionauth.api.service.system.KeyService;
import io.fusionauth.api.util.ClaimTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.JWTConfiguration;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.jwt.domain.JWT;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultJWTClaimValidator implements JWTClaimValidator {
  private static final Logger logger = LoggerFactory.getLogger(DefaultJWTClaimValidator.class);
  
  private final KeyCache keyCache;
  
  @Inject
  public DefaultJWTClaimValidator(KeyCache paramKeyCache) {
    this.keyCache = paramKeyCache;
  }
  
  public JWTClaimValidator.ClaimValidationResult validateForApplication(@Nonnull JWT paramJWT, @Nonnull Tenant paramTenant, @Nonnull Application paramApplication, boolean paramBoolean) {
    UUID uUID = paramApplication.id;
    if (audIsInvalid(paramJWT, uUID))
      return new JWTClaimValidator.ClaimValidationResult.InvalidAudience(); 
    if (tenantClaimsAreInvalid(paramJWT, paramTenant))
      return new JWTClaimValidator.ClaimValidationResult.Mismatch("The token claims do not match the resolved tenant/application."); 
    boolean bool = JWTType.is(paramJWT, JWTType.IdToken);
    JWTConfiguration jWTConfiguration = paramTenant.lookupJWTConfiguration(paramApplication);
    boolean bool1 = (bool && hasShadowIdTokenKey(jWTConfiguration)) ? true : false;
    if (kidIsInvalid(paramJWT, uUID, bool, bool1, paramBoolean, 
        eligibleApplicationKeyIds(paramTenant, paramApplication, bool)))
      return new JWTClaimValidator.ClaimValidationResult.Mismatch("The [kid] does not resolve to a configured verification key for the token type."); 
    return new JWTClaimValidator.ClaimValidationResult.Valid();
  }
  
  public JWTClaimValidator.ClaimValidationResult validateForEntity(@Nonnull JWT paramJWT, @Nonnull Tenant paramTenant, @Nonnull Entity paramEntity) {
    if (tenantClaimsAreInvalid(paramJWT, paramTenant))
      return new JWTClaimValidator.ClaimValidationResult.Mismatch("The token claims do not match the resolved tenant."); 
    if (kidIsInvalid(paramJWT, null, false, false, false, eligibleEntityKeyIds(paramTenant, paramEntity)))
      return new JWTClaimValidator.ClaimValidationResult.Mismatch("The [kid] does not resolve to a configured verification key for the entity."); 
    return new JWTClaimValidator.ClaimValidationResult.Valid();
  }
  
  public JWTClaimValidator.ClaimValidationResult validateForTenant(@Nonnull JWT paramJWT, @Nonnull Tenant paramTenant) {
    if (tenantClaimsAreInvalid(paramJWT, paramTenant))
      return new JWTClaimValidator.ClaimValidationResult.Mismatch("The token claims do not match the resolved tenant."); 
    boolean bool = JWTType.is(paramJWT, JWTType.IdToken);
    if (kidIsInvalid(paramJWT, null, bool, false, false, eligibleTenantKeyIds(paramTenant, bool)))
      return new JWTClaimValidator.ClaimValidationResult.Mismatch("The [kid] does not resolve to a configured verification key for the token type."); 
    return new JWTClaimValidator.ClaimValidationResult.Valid();
  }
  
  private boolean audIsInvalid(@Nonnull JWT paramJWT, @Nonnull UUID paramUUID) {
    if (!ClaimTools.audienceContainsClientId(paramJWT.audience, paramUUID.toString())) {
      logger.debug("Invalid JWT. Expected aud [{}] but found [{}]", paramUUID, paramJWT.audience);
      return true;
    } 
    return false;
  }
  
  private Set<UUID> eligibleApplicationKeyIds(@Nonnull Tenant paramTenant, @Nullable Application paramApplication, boolean paramBoolean) {
    return eligibleOrdinaryKeyIds(paramTenant.lookupJWTConfiguration(paramApplication), paramBoolean);
  }
  
  private Set<UUID> eligibleEntityKeyIds(Tenant paramTenant, Entity paramEntity) {
    EntityType.EntityJWTConfiguration entityJWTConfiguration = (paramEntity.type == null) ? null : paramEntity.type.jwtConfiguration;
    if (entityJWTConfiguration == null || !entityJWTConfiguration.enabled)
      return eligibleKeyIds(paramTenant.jwtConfiguration, false); 
    HashSet<UUID> hashSet = new HashSet();
    if (entityJWTConfiguration.accessTokenKeyId != null) {
      hashSet.add(entityJWTConfiguration.accessTokenKeyId);
    } else {
      hashSet.addAll(eligibleKeyIds(paramTenant.jwtConfiguration, false));
    } 
    hashSet.addAll(entityJWTConfiguration.accessTokenVerificationKeyIds);
    return hashSet;
  }
  
  private Set<UUID> eligibleKeyIds(@Nonnull JWTConfiguration paramJWTConfiguration, boolean paramBoolean) {
    HashSet<UUID> hashSet = new HashSet();
    UUID uUID = paramBoolean ? paramJWTConfiguration.idTokenKeyId : paramJWTConfiguration.accessTokenKeyId;
    List<UUID> list = paramBoolean ? paramJWTConfiguration.idTokenVerificationKeyIds : paramJWTConfiguration.accessTokenVerificationKeyIds;
    if (uUID != null)
      hashSet.add(uUID); 
    hashSet.addAll(list);
    return hashSet;
  }
  
  private Set<UUID> eligibleOrdinaryKeyIds(@Nonnull JWTConfiguration paramJWTConfiguration, boolean paramBoolean) {
    HashSet<UUID> hashSet = new HashSet();
    UUID uUID = paramBoolean ? paramJWTConfiguration.idTokenKeyId : paramJWTConfiguration.accessTokenKeyId;
    List<UUID> list = paramBoolean ? paramJWTConfiguration.idTokenVerificationKeyIds : paramJWTConfiguration.accessTokenVerificationKeyIds;
    if (uUID != null && !KeyService.ClientSecretShadowKeys.contains(uUID))
      hashSet.add(uUID); 
    Objects.requireNonNull(hashSet);
    list.stream().filter(paramUUID -> (paramUUID != null && !KeyService.ClientSecretShadowKeys.contains(paramUUID))).forEach(hashSet::add);
    return hashSet;
  }
  
  private Set<UUID> eligibleTenantKeyIds(@Nonnull Tenant paramTenant, boolean paramBoolean) {
    return eligibleOrdinaryKeyIds(paramTenant.jwtConfiguration, paramBoolean);
  }
  
  private boolean hasShadowIdTokenKey(@Nonnull JWTConfiguration paramJWTConfiguration) {
    Objects.requireNonNull(KeyService.ClientSecretShadowKeys);
    return (KeyService.ClientSecretShadowKeys.contains(paramJWTConfiguration.idTokenKeyId) || paramJWTConfiguration.idTokenVerificationKeyIds.stream().anyMatch(KeyService.ClientSecretShadowKeys::contains));
  }
  
  private boolean kidIsInvalid(JWT paramJWT, UUID paramUUID, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, Set<UUID> paramSet) {
    String str = (paramJWT.header == null) ? null : paramJWT.header.getString("kid");
    if (str == null)
      return true; 
    if (paramBoolean3 && paramBoolean1 && paramBoolean2 && paramUUID != null && paramUUID.toString().equals(str))
      return false; 
    Key key = this.keyCache.getVerifierKeyByKid(str);
    if (key == null)
      return true; 
    if (KeyService.ClientSecretShadowKeys.contains(key.id))
      return true; 
    return !paramSet.contains(key.id);
  }
  
  private boolean tenantClaimsAreInvalid(@Nonnull JWT paramJWT, @Nonnull Tenant paramTenant) {
    String str = paramJWT.getString("tid");
    if (!paramTenant.id.toString().equals(str)) {
      logger.debug("Invalid JWT. Expected tid [{}] but found [{}]", paramTenant.id, str);
      return true;
    } 
    return false;
  }
}
