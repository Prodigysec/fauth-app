package io.fusionauth.app.action.ajax.identityProvider.key;

import com.google.inject.Inject;
import io.fusionauth.api.domain.IdentityProviderKeyType;
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
  
  public IdentityProviderKeyType type;
  
  @Inject
  protected SearchAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.keySearchResponse = SearchFrontendService.searchForSigningKeys(this.name, this.client, paramKey -> Boolean.valueOf(!isValidForType(paramKey, this.type)));
    return "render-json";
  }
  
  private boolean isValidForType(Key paramKey, IdentityProviderKeyType paramIdentityProviderKeyType) {
    switch (paramIdentityProviderKeyType) {
      default:
        throw new MatchException(null, null);
      case ExternalJwtVerification:
      
      case Samlv2ResponseVerification:
        break;
    } 
    return 
      
      KeyValidator.validForSAMLVerification(paramKey);
  }
}
