package io.fusionauth.app.action.oauth2.device;

import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.app.action.NoStoreJSONBaseAction;
import io.fusionauth.domain.oauth2.OAuthResponse;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class ValidateAction extends NoStoreJSONBaseAction {
  private final OAuthService oauthService;
  
  public String client_id;
  
  @JSONResponse
  public OAuthResponse response;
  
  public UUID tenantId;
  
  public String user_code;
  
  @Inject
  public ValidateAction(OAuthService paramOAuthService) {
    this.oauthService = paramOAuthService;
  }
  
  public String get() {
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.response = (this.oauthService.validateUserCodeRequest(this.tenantId, this.client_id, this.user_code)).error;
    if (this.response != null)
      throw new ErrorException("input", false); 
  }
}
