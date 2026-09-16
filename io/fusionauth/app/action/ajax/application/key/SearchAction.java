package io.fusionauth.app.action.ajax.application.key;

import com.google.inject.Inject;
import io.fusionauth.api.domain.ApplicationKeyType;
import io.fusionauth.api.util.KeyValidator;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.search.SearchFrontendService;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.KeySearchResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, constraints = {"admin"})
public class SearchAction extends BaseAJAXAction {
  @JSONResponse
  public KeySearchResponse keySearchResponse = new KeySearchResponse();
  
  public String name;
  
  public ApplicationKeyType type;
  
  @Inject
  protected SearchAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.keySearchResponse = SearchFrontendService.searchForSigningKeys(this.name, this.client, paramKey -> Boolean.valueOf(!isValidForType(paramKey, this.type)));
    return "render-json";
  }
  
  private boolean isValidForType(Key paramKey, ApplicationKeyType paramApplicationKeyType) {
    switch (paramApplicationKeyType) {
      default:
        throw new MatchException(null, null);
      case AccessTokenSigning:
      
      case AccessTokenVerification:
      
      case IdTokenSigning:
      
      case IdTokenVerification:
      
      case Samlv2Verification:
      case Samlv2LogoutVerification:
        break;
    } 
    return 



      
      KeyValidator.validForSAMLVerification(paramKey);
  }
}
