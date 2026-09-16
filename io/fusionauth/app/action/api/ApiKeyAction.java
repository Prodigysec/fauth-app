package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.domain.api.APIKeyBridge;
import io.fusionauth.api.service.system.APIKeyReaderService;
import io.fusionauth.api.service.system.APIKeyService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.api.APIKeyRequest;
import io.fusionauth.domain.api.APIKeyResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{apiKeyId}", requiresAuthentication = true, scheme = {"api"})
public class ApiKeyAction extends BaseTenantAPIAction implements Patchable {
  private final APIKey actualAPIKey;
  
  private final APIKeyReaderService apiKeyReader;
  
  private final APIKeyService apiKeyService;
  
  @PreParameter
  public UUID apiKeyId;
  
  @JSONPatch
  @JSONRequest
  public APIKeyRequest request = new APIKeyRequest();
  
  @JSONResponse
  public APIKeyResponse response = new APIKeyResponse();
  
  private APIKeyService.ValidationResult result;
  
  @Inject
  public ApiKeyAction(FrontEndSupport paramFrontEndSupport, APIKeyReaderService paramAPIKeyReaderService, APIKeyService paramAPIKeyService, AuthenticationKeyCache paramAuthenticationKeyCache) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.apiKeyReader = paramAPIKeyReaderService;
    this.apiKeyService = paramAPIKeyService;
    String str = paramFrontEndSupport.request.getHeader("Authorization");
    this.actualAPIKey = (str != null) ? APIKeyBridge.convert(paramAuthenticationKeyCache.get(str)) : null;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.apiKeyService.delete(this.result.existing);
    return "success";
  }
  
  public String get() {
    if (this.result.apiKey == null)
      return "missing"; 
    this.response = new APIKeyResponse(this.result.apiKey);
    return "render";
  }
  
  public void loadExisting() {
    if (this.apiKeyId != null) {
      UUID uUID = tenantIdWasSpecified() ? getOptionalTenantId() : null;
      this.request.apiKey = this.apiKeyReader.retrieveById(uUID, this.apiKeyId);
    } 
  }
  
  public String post() {
    this.apiKeyService.create(this.result.apiKey);
    this.response = new APIKeyResponse(this.result.apiKey);
    return "render";
  }
  
  public String put() {
    this.request.apiKey.keyManager = this.result.existing.keyManager;
    this.apiKeyService.update(this.result.existing, this.request.apiKey);
    this.response = new APIKeyResponse(this.request.apiKey);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.apiKeyId == null)
      this.frontEndSupport.addFieldError("apiKeyId", "[missing]apiKeyId", new Object[0]); 
    this.result = this.apiKeyService.validateDelete(getOptionalTenant(), tenantIdWasSpecified(), this.apiKeyId, this.actualAPIKey);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.apiKeyId == null)
      this.frontEndSupport.addFieldError("apiKeyId", "[missing]apiKeyId", new Object[0]); 
    this.result = this.apiKeyService.validateGet(getOptionalTenant(), tenantIdWasSpecified(), this.apiKeyId, this.actualAPIKey);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.sourceKeyId != null) {
      if (this.request.apiKey == null)
        this.request.apiKey = new APIKey(); 
      this.request.apiKey.id = this.apiKeyId;
      this.result = this.apiKeyService.validateCopy(getOptionalTenant(), tenantIdWasSpecified(), this.request.apiKey, this.request.sourceKeyId, this.actualAPIKey);
    } else {
      if (this.request.apiKey == null) {
        this.frontEndSupport.addFieldError("apiKey", "[missing]apiKey", new Object[0]);
        return;
      } 
      this.request.apiKey.id = this.apiKeyId;
      this.request.apiKey.normalize();
      this.result = this.apiKeyService.validateCreate(getOptionalTenant(), tenantIdWasSpecified(), this.request.apiKey, this.actualAPIKey);
    } 
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validateUpdate() {
    if (this.request.apiKey == null) {
      this.frontEndSupport.addFieldError("apiKey", "[missing]apiKey", new Object[0]);
      return;
    } 
    this.request.apiKey.id = this.apiKeyId;
    this.request.apiKey.normalize();
    this.result = this.apiKeyService.validateUpdate(getOptionalTenant(), tenantIdWasSpecified(), this.request.apiKey, this.actualAPIKey, this.request.sourceKeyId);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
