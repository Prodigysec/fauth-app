package io.fusionauth.api.security;

import com.google.inject.Inject;
import io.fusionauth.api.domain.EntityMapper;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.jwt.FusionAuthJWTDecoder;
import io.fusionauth.api.service.jwt.JWTClaimValidator;
import io.fusionauth.api.service.jwt.ValidatedJWTResult;
import io.fusionauth.api.util.ClaimTools;
import io.fusionauth.api.util.UUIDTools;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.Tenant;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.domain.JWT;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.security.JWTConstraintsValidator;

public class SCIMConstraintValidator implements JWTConstraintsValidator {
  private final EntityMapper entityMapper;
  
  private final JWTClaimValidator jwtClaimValidator;
  
  private final HTTPRequest request;
  
  private final TenantCache tenantCache;
  
  @Inject
  public SCIMConstraintValidator(EntityMapper paramEntityMapper, JWTClaimValidator paramJWTClaimValidator, HTTPRequest paramHTTPRequest, TenantCache paramTenantCache) {
    this.entityMapper = paramEntityMapper;
    this.jwtClaimValidator = paramJWTClaimValidator;
    this.request = paramHTTPRequest;
    this.tenantCache = paramTenantCache;
  }
  
  public boolean validate(JWT paramJWT, String[] paramArrayOfString) {
    ValidatedJWTResult validatedJWTResult = FusionAuthJWTDecoder.validateConstraints(paramJWT, FusionAuthJWTDecoder.JWTConstraints.SCIMServerAccessToken);
    if (!validatedJWTResult.valid)
      return false; 
    Object object = paramJWT.getObject("use");
    if (object instanceof String) {
      String str = (String)object;
      if (str.equals("scim_server")) {
        UUID uUID1 = UUIDTools.fromString(paramJWT.getString("tid"));
        if (uUID1 == null)
          return false; 
        String str1 = ClaimTools.getAudience(paramJWT);
        UUID uUID2 = UUIDTools.fromString(paramJWT.subject);
        UUID uUID3 = UUIDTools.fromString(str1);
        if (uUID2 == null || uUID3 == null)
          return false; 
        Entity entity1 = this.entityMapper.retrieveEntityById(uUID1, uUID2);
        if (entity1 == null)
          return false; 
        Tenant tenant = (Tenant)this.tenantCache.get(entity1.tenantId);
        if (tenant == null)
          return false; 
        UUID uUID4 = UUIDTools.fromString(this.request.getHeader(FusionAuthClient.TENANT_ID_HEADER));
        if (uUID4 != null && !tenant.id.equals(uUID4))
          return false; 
        if (!(this.jwtClaimValidator.validateForEntity(paramJWT, tenant, entity1) instanceof JWTClaimValidator.ClaimValidationResult.Valid))
          return false; 
        if (!tenant.issuer.equals(paramJWT.issuer))
          return false; 
        Entity entity2 = this.entityMapper.retrieveEntityById(uUID1, uUID3);
        if (entity2 == null || !entity2.type.id.equals(tenant.scimServerConfiguration.serverEntityTypeId))
          return false; 
        if (!entity1.type.id.equals(tenant.scimServerConfiguration.clientEntityTypeId))
          return false; 
        if (paramArrayOfString.length == 0) {
          this.request.setAttribute("clientEntity", entity1);
          this.request.setAttribute("clientEntityTenant", tenant);
          return true;
        } 
        Object object1 = paramJWT.getObject("permissions");
        if (object1 instanceof Map) {
          Map map = (Map)object1;
          Object object2 = map.get(str1);
          if (object2 instanceof List) {
            List list = (List)object2;
            for (String str2 : paramArrayOfString) {
              if (list.contains(str2)) {
                this.request.setAttribute("clientEntity", entity1);
                this.request.setAttribute("clientEntityTenant", tenant);
                return true;
              } 
            } 
          } 
        } 
        return false;
      } 
    } 
    return false;
  }
}
