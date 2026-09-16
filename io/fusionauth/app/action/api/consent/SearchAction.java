package io.fusionauth.app.action.api.consent;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.consent.ConsentService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.ConsentSearchRequest;
import io.fusionauth.domain.api.ConsentSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.ConsentSearchCriteria;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class SearchAction extends BaseSearchAPIAction<ConsentSearchCriteria> {
  private final ConsentService consentService;
  
  @JSONRequest
  public ConsentSearchRequest request = new ConsentSearchRequest();
  
  @JSONResponse
  public ConsentSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, ConsentService paramConsentService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.consentService = paramConsentService;
  }
  
  public String name() {
    return (criteria()).name;
  }
  
  public void setName(String paramString) {
    (criteria()).name = paramString;
  }
  
  protected ConsentSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    this.response = new ConsentSearchResponse(this.consentService.search(this.request.search));
    return "render";
  }
}
