package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.identity.IdentityProviderReaderService;
import io.fusionauth.api.service.identity.IdentityProviderService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.IdentityProviderRequest;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.apache.ibatis.exceptions.PersistenceException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{identityProviderId}", scheme = {"api"}, requiresAuthentication = true)
public class IdentityProviderAction extends BaseTenantAPIAction implements Patchable {
  private final IdentityProviderReaderService identityProviderReader;
  
  private final IdentityProviderService identityProviderService;
  
  @PreParameter
  public UUID identityProviderId;
  
  @JSONPatch
  @JSONRequest
  public IdentityProviderRequest request = new IdentityProviderRequest();
  
  @JSONResponse
  public IdentityProviderResponse response;
  
  public UUID tenantId;
  
  @PreParameter
  public IdentityProviderType type;
  
  private ZonedDateTime existingLastUpdateInstant;
  
  private IdentityProviderService.ValidationResult result;
  
  @Inject
  public IdentityProviderAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, IdentityProviderReaderService paramIdentityProviderReaderService, IdentityProviderService paramIdentityProviderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.identityProviderReader = paramIdentityProviderReaderService;
    this.identityProviderService = paramIdentityProviderService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.identityProviderService.delete(this.result.existing);
    return "success";
  }
  
  public String get() {
    if (this.identityProviderId != null) {
      BaseIdentityProvider<?> baseIdentityProvider = this.identityProviderReader.retrieveById(this.tenantId, this.identityProviderId);
      if (baseIdentityProvider == null)
        return "missing"; 
      this.response = new IdentityProviderResponse(baseIdentityProvider);
    } else if (this.type != null) {
      this.response = new IdentityProviderResponse(this.identityProviderReader.retrieveByType(this.tenantId, this.type));
    } else {
      this.response = new IdentityProviderResponse(this.identityProviderReader.retrieveAll(this.tenantId));
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.identityProviderId == null && this.type != null)
      this.identityProviderId = this.type.getId(); 
    if (this.identityProviderId != null) {
      this.request.identityProvider = this.identityProviderReader.retrieveById(this.tenantId, this.identityProviderId);
      if (this.request.identityProvider != null)
        this.existingLastUpdateInstant = this.request.identityProvider.lastUpdateInstant; 
    } 
  }
  
  public String post() {
    try {
      this.identityProviderService.create(this.result.identityProvider);
      this.response = new IdentityProviderResponse(this.result.identityProvider);
      return "render";
    } catch (PersistenceException persistenceException) {
      if (isConstraintViolation(persistenceException))
        return conflict(); 
      throw persistenceException;
    } 
  }
  
  @PostParameterMethod
  public void postParameter() {
    if (tenantIdWasSpecified())
      this.tenantId = getOptionalTenantId(); 
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    int i = this.identityProviderService.update(this.result.existing, this.result.identityProvider, this.existingLastUpdateInstant);
    if (i == 0)
      return retryableConflict(); 
    this.response = new IdentityProviderResponse(this.result.identityProvider);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT", "PATCH"})
  public void validate() {
    if (this.request.identityProvider == null) {
      this.frontEndSupport.addFieldError("identityProvider", "[missing]identityProvider", new Object[0]);
      return;
    } 
    this.request.identityProvider.id = this.identityProviderId;
    if (tenantIdWasSpecified())
      this.request.identityProvider.tenantId = getOptionalTenantId(); 
    this.request.identityProvider.normalize();
    this.result = this.identityProviderService.validate(this.tenantId, this.request.identityProvider, this.frontEndSupport.isPOST());
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.type != null)
      this.identityProviderId = this.type.getId(); 
    this.result = this.identityProviderService.validateDelete(this.tenantId, this.identityProviderId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  private boolean isConstraintViolation(PersistenceException paramPersistenceException) {
    Throwable throwable = paramPersistenceException.getCause();
    while (throwable != null) {
      if (throwable instanceof java.sql.SQLIntegrityConstraintViolationException || throwable.getMessage().contains("duplicate key value"))
        return true; 
      throwable = throwable.getCause();
    } 
    return false;
  }
}
