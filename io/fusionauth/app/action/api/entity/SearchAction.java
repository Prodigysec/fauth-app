package io.fusionauth.app.action.api.entity;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.entity.EntityService;
import io.fusionauth.app.action.api.BaseSearchTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.api.EntitySearchRequest;
import io.fusionauth.domain.api.EntitySearchResponse;
import io.fusionauth.domain.search.BaseElasticSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.SortField;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(value = "{queryString}", requiresAuthentication = true, scheme = {"api"})
public class SearchAction extends BaseSearchTenantAPIAction {
  private final EntityService entityService;
  
  @JSONRequest
  public EntitySearchRequest request = new EntitySearchRequest();
  
  @JSONResponse
  public EntitySearchResponse response;
  
  @Inject
  public SearchAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, EntityService paramEntityService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.entityService = paramEntityService;
  }
  
  public String put() {
    this.entityService.refreshSearchIndex();
    return "success";
  }
  
  protected BaseElasticSearchCriteria criteria() {
    return (this.request != null) ? this.request.search : null;
  }
  
  protected String search() {
    this.response = new EntitySearchResponse();
    if (this.ids != null && this.ids.size() > 0) {
      this.response.entities = this.entityService.retrieveByIds(getOptionalTenantId(), this.ids);
      this.response.total = this.response.entities.size();
      this.response.totalEqualToActual = true;
    } else {
      SearchResults<Entity> searchResults;
      if (this.query != null) {
        searchResults = this.entityService.searchByQuery(getOptionalTenantId(), this.query, this.startRow, this.sortFields, this.numberOfResults, this.accurateTotal, this.lastSort, this.pointInTimeId);
      } else {
        searchResults = this.entityService.searchByQueryString(getOptionalTenantId(), this.queryString, this.numberOfResults, this.startRow, this.sortFields, this.accurateTotal, this.lastSort, this.pointInTimeId);
      } 
      this.response.entities = searchResults.results;
      this.response.nextResults = searchResults.nextResults;
      this.response.total = searchResults.total;
      this.response.totalEqualToActual = searchResults.totalEqualToActual;
    } 
    return "render";
  }
  
  protected Errors validateSearchQuery(UUID paramUUID, String paramString1, String paramString2, List<SortField> paramList, int paramInt1, int paramInt2) {
    return this.entityService.validateSearchQuery(paramUUID, paramString1, paramString2, paramList, paramInt1, paramInt2);
  }
}
