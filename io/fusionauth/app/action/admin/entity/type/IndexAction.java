package io.fusionauth.app.action.admin.entity.type;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.api.EntityTypeSearchRequest;
import io.fusionauth.domain.api.EntityTypeSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.EntityTypeSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "entity_manager"})
public class IndexAction extends BaseSearchAction<EntityType, EntityTypeSearchCriteria> {
  public List<EntityType> entityTypes;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected EntityTypeSearchCriteria defaultSearchCriteria() {
    return new EntityTypeSearchCriteria();
  }
  
  protected SearchResults<EntityType> search() {
    EntityTypeSearchResponse entityTypeSearchResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchEntityTypes(new EntityTypeSearchRequest(this.s)));
    return new SearchResults<>(entityTypeSearchResponse.entityTypes, entityTypeSearchResponse.total);
  }
}
