package io.fusionauth.app.action.ajax.entity.grant;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.domain.Pagination;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EntityGrant;
import io.fusionauth.domain.api.EntityGrantSearchRequest;
import io.fusionauth.domain.api.EntityGrantSearchResponse;
import io.fusionauth.domain.search.EntityGrantSearchCriteria;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "entity_manager", "user_manager", "user_support_manager", "user_support_viewer"})
public class SearchAction extends BaseAJAXAction {
  public int currentPage;
  
  public long firstResult;
  
  public long lastResult;
  
  public int numberOfPages;
  
  public List<EntityGrant> results;
  
  public EntityGrantSearchCriteria s = new EntityGrantSearchCriteria();
  
  public long total;
  
  @Inject
  public SearchAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.s.name != null)
      this.s.name = this.s.name.trim(); 
    EntityGrantSearchResponse entityGrantSearchResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchEntityGrants(new EntityGrantSearchRequest(this.s)));
    this.results = entityGrantSearchResponse.grants;
    this.total = entityGrantSearchResponse.total;
    calculatePagination();
    return "render";
  }
  
  protected void calculatePagination() {
    boolean bool = (this.results == null) ? false : this.results.size();
    Pagination pagination = new Pagination(this.s.startRow, this.s.numberOfResults, bool, this.total);
    this.numberOfPages = pagination.numberOfPages;
    this.currentPage = pagination.currentPage;
    this.firstResult = pagination.firstResult;
    this.lastResult = pagination.lastResult;
  }
}
