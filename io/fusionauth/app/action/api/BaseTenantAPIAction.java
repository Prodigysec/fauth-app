package io.fusionauth.app.action.api;

import com.inversoft.authentication.api.domain.AuthenticationKey;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.InvalidTenantIdException;
import io.fusionauth.api.service.TenantIdRequired;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import org.primeframework.mvc.security.UnauthorizedException;

public abstract class BaseTenantAPIAction extends BaseAPIAction {
  protected final AuthenticationKeyCache authenticationKeyCache;
  
  protected Tenant tenant;
  
  protected boolean useTenantCache;
  
  private boolean tenantIdWasSpecified = false;
  
  protected BaseTenantAPIAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache) {
    super(paramFrontEndSupport);
    this.authenticationKeyCache = paramAuthenticationKeyCache;
  }
  
  @PreParameterMethod
  public void resolveTenant() {
    UUID uUID = ActionTools.resolveTenantIdFromHeader(this.frontEndSupport.request).orElse(null);
    String str = this.frontEndSupport.request.getHeader("Authorization");
    if (str != null) {
      AuthenticationKey authenticationKey = this.authenticationKeyCache.get(str);
      if (authenticationKey != null && authenticationKey.tenantId != null) {
        if (uUID != null && !authenticationKey.tenantId.equals(uUID))
          throw new UnauthorizedException(); 
        uUID = authenticationKey.tenantId;
      } 
    } 
    if (uUID == null) {
      uUID = askActionForTenantId();
    } else {
      this.tenantIdWasSpecified = true;
    } 
    if (uUID == null && resolveDefaultTenant()) {
      Objects.requireNonNull(this.frontEndSupport.tenantReader);
      this.tenant = this.useTenantCache ? this.frontEndSupport.tenantCache.getDefaultOrNull(this.frontEndSupport.tenantReader::retrieveById) : this.frontEndSupport.tenantReader.retrieveDefaultOrNull();
    } else if (uUID != null) {
      Objects.requireNonNull(this.frontEndSupport.tenantReader);
      this.tenant = this.useTenantCache ? this.frontEndSupport.tenantCache.get(uUID, this.frontEndSupport.tenantReader::retrieveById) : this.frontEndSupport.tenantReader.retrieveById(uUID);
      if (this.tenant == null)
        handleInvalidTenantId(new InvalidTenantIdException(uUID)); 
    } 
  }
  
  protected UUID askActionForTenantId() {
    return null;
  }
  
  protected void assertTenantResolved() {
    getTenant();
  }
  
  protected void conditionallyUpdateTenant(Tenant paramTenant) {
    if (this.tenant != null)
      return; 
    this.tenant = paramTenant;
  }
  
  protected Tenant getAppTenant(Application paramApplication) {
    return paramApplication.universalConfiguration.universal ? null : getTenant();
  }
  
  protected AuthenticationKey getAuthenticationKey() {
    String str = this.frontEndSupport.request.getHeader("Authorization");
    if (str != null)
      return this.authenticationKeyCache.get(str); 
    return null;
  }
  
  protected Tenant getOptionalTenant() {
    return this.tenant;
  }
  
  protected UUID getOptionalTenantId() {
    return (this.tenant != null) ? this.tenant.id : null;
  }
  
  protected Tenant getTenant() {
    if (this.tenant == null)
      throw new TenantIdRequired(); 
    return this.tenant;
  }
  
  protected void handleInvalidTenantId(InvalidTenantIdException paramInvalidTenantIdException) {
    throw paramInvalidTenantIdException;
  }
  
  protected boolean keyIsTenantScoped() {
    AuthenticationKey authenticationKey = getAuthenticationKey();
    if (authenticationKey != null)
      return (authenticationKey.tenantId != null); 
    return false;
  }
  
  protected boolean resolveDefaultTenant() {
    return true;
  }
  
  protected boolean tenantIdWasSpecified() {
    return this.tenantIdWasSpecified;
  }
  
  protected boolean tenantScopedKeyInvalidForUser(Application paramApplication, User paramUser) {
    AuthenticationKey authenticationKey = getAuthenticationKey();
    if (authenticationKey == null || authenticationKey.tenantId == null)
      return false; 
    if (paramApplication != null && paramApplication.universalConfiguration.universal)
      return (paramUser == null || !paramUser.tenantId.equals(authenticationKey.tenantId)); 
    return false;
  }
}
