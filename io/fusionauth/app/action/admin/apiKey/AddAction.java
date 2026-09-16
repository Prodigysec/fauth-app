package io.fusionauth.app.action.admin.apiKey;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.SecurityTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.security.SecurityTools;
import io.fusionauth.api.service.system.APIKeyService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.api.APIKeyResponse;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "api_key_manager"})
public class AddAction extends BaseFormAction {
  private final APIKeyService apiKeyService;
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport, APIKeyService paramAPIKeyService, Map<String, APIKeyService.APIEndpointScope> paramMap) {
    super(paramFrontEndSupport, paramMap);
    this.apiKeyService = paramAPIKeyService;
  }
  
  public String get() {
    if (this.apiKeyId != null) {
      this.apiKey = ((APIKeyResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveAPIKey(this.apiKeyId))).apiKey;
      createOrUpdateDescriptionMetaData();
      this.apiKeyId = null;
      this.apiKey.id = null;
    } 
    this.apiKey.key = SecurityTools.secureRandom(42);
    return "input";
  }
  
  public String post() {
    this.apiKey.id = this.apiKeyId;
    this.apiKeyService.create(this.apiKey);
    if (this.apiKey.retrievable) {
      this.apiKey.key = SecurityTools.lastN(this.apiKey.key, 7);
      writeAuditLog("Created the API key with Id [" + String.valueOf(this.apiKey.id) + "] and a key ending [" + this.apiKey.key + "].");
    } else {
      writeAuditLog("Created the API key with Id [" + String.valueOf(this.apiKey.id) + "] and name [" + this.apiKey.name + "].");
    } 
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    this.apiKey.tenantId = this.tenantId;
    APIKeyService.ValidationResult validationResult = this.apiKeyService.validateCreateForAdminUI(null, false, this.apiKey);
    this.frontEndSupport.transfer(validationResult.errors.add((new Validator())
          .notMissing(this.apiKey.key, "apiKey.key", new Object[0])
          .done()));
  }
  
  private void createOrUpdateDescriptionMetaData() {
    if (this.apiKey.metaData == null)
      this.apiKey.metaData = new APIKey.APIKeyMetaData(); 
    this.apiKey.metaData.attributes.merge("description", " - copy", String::concat);
  }
}
