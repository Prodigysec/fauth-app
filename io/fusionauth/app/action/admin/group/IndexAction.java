package io.fusionauth.app.action.admin.group;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.api.GroupResponse;
import io.fusionauth.domain.api.GroupSearchRequest;
import io.fusionauth.domain.api.GroupSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.GroupSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "group_manager"})
public class IndexAction extends BaseSearchAction<Group, GroupSearchCriteria> {
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected GroupSearchCriteria defaultSearchCriteria() {
    return new GroupSearchCriteria();
  }
  
  protected SearchResults<Group> search() {
    UUID uUID = parseUUID(this.s.name);
    if (uUID != null) {
      ClientResponse<GroupResponse, Errors> clientResponse = this.client.setTenantId(this.s.tenantId).retrieveGroup(uUID);
      if (clientResponse.wasSuccessful())
        return new SearchResults<>(List.of(((GroupResponse)clientResponse.getSuccessResponse()).group), 1L); 
      return null;
    } 
    GroupSearchResponse groupSearchResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchGroups(new GroupSearchRequest(this.s)));
    return new SearchResults<>(groupSearchResponse.groups, groupSearchResponse.total);
  }
}
