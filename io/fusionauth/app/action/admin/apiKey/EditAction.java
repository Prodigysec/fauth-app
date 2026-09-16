package io.fusionauth.app.action.admin.apiKey;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.security.SecurityTools;
import io.fusionauth.api.service.system.APIKeyService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.api.APIKeyResponse;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{apiKeyId}", requiresAuthentication = true, constraints = {"admin", "api_key_manager"})
public class EditAction extends BaseFormAction {
  private final APIKeyService apiKeyService;
  
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport, APIKeyService paramAPIKeyService, Map<String, APIKeyService.APIEndpointScope> paramMap) {
    super(paramFrontEndSupport, paramMap);
    this.apiKeyService = paramAPIKeyService;
  }
  
  public String get() {
    this.apiKey = ((APIKeyResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveAPIKey(this.apiKeyId))).apiKey;
    if (this.apiKey.key != null)
      this.apiKey.key = "…" + this.apiKey.key.substring(this.apiKey.key.length() - 7); 
    return "input";
  }
  
  public String post() {
    APIKey aPIKey = ((APIKeyResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveAPIKey(this.apiKeyId))).apiKey;
    this.apiKeyService.update(aPIKey, this.apiKey);
    if (this.apiKey.retrievable) {
      aPIKey.key = SecurityTools.lastN(aPIKey.key, 7);
      this.apiKey.key = SecurityTools.lastN(this.apiKey.key, 7);
      writeAuditLogForUpdate("Updated the API key with Id [" + String.valueOf(this.apiKeyId) + "] and key ending in [" + aPIKey.key + "]", aPIKey, this.apiKey);
    } else {
      writeAuditLogForUpdate("Updated the hashed API key with Id [" + String.valueOf(this.apiKeyId) + "] and name [" + this.apiKey.name + "]", aPIKey, this.apiKey);
    } 
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    this.apiKey.id = this.apiKeyId;
    APIKeyService.ValidationResult validationResult = this.apiKeyService.validateUpdateForAdminUI(null, false, this.apiKey, null);
    if (validationResult.errors != null)
      validationResult.errors.fieldErrors.remove("apiKeyId"); 
    this.frontEndSupport.transfer(validationResult.errors);
  }
}
