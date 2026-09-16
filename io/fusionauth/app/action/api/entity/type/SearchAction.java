package io.fusionauth.app.action.api.entity.type;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.entity.EntityService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.EntityTypeSearchRequest;
import io.fusionauth.domain.api.EntityTypeSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.EntityTypeSearchCriteria;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class SearchAction extends BaseSearchAPIAction<EntityTypeSearchCriteria> {
  @JSONRequest
  public final EntityTypeSearchRequest request = new EntityTypeSearchRequest();
  
  private final EntityService entityService;
  
  @JSONResponse
  public EntityTypeSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, EntityService paramEntityService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.entityService = paramEntityService;
  }
  
  public String getName() {
    return (criteria()).name;
  }
  
  public void setName(String paramString) {
    (criteria()).name = paramString;
  }
  
  protected EntityTypeSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    this.response = new EntityTypeSearchResponse(this.entityService.searchTypes(this.request.search));
    return "render";
  }
}
