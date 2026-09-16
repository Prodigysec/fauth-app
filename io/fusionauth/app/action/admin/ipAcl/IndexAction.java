package io.fusionauth.app.action.admin.ipAcl;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.api.IPAccessControlListSearchRequest;
import io.fusionauth.domain.api.IPAccessControlListSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.IPAccessControlListSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "acl_manager"})
public class IndexAction extends BaseSearchAction<IPAccessControlList, IPAccessControlListSearchCriteria> {
  public List<IPAccessControlList> ipAccessControlLists;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected IPAccessControlListSearchCriteria defaultSearchCriteria() {
    return new IPAccessControlListSearchCriteria();
  }
  
  protected SearchResults<IPAccessControlList> search() {
    IPAccessControlListSearchResponse iPAccessControlListSearchResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchIPAccessControlLists(new IPAccessControlListSearchRequest(this.s)));
    return new SearchResults<>(iPAccessControlListSearchResponse.ipAccessControlLists, iPAccessControlListSearchResponse.total);
  }
}
