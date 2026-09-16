package io.fusionauth.app.action.admin.application.inactive;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ObjectState;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.ApplicationSearchRequest;
import io.fusionauth.domain.api.ApplicationSearchResponse;
import io.fusionauth.domain.search.ApplicationSearchCriteria;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager"})
public class IndexAction extends BaseSearchAction<Application, ApplicationSearchCriteria> {
  @FTLVariable
  public final UUID tenantManagerId;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport, @TenantManagerApplicationId UUID paramUUID) {
    super(paramFrontEndSupport);
    this.tenantManagerId = paramUUID;
  }
  
  protected ApplicationSearchCriteria defaultSearchCriteria() {
    return (new ApplicationSearchCriteria()).with(paramApplicationSearchCriteria -> paramApplicationSearchCriteria.state = ObjectState.Inactive);
  }
  
  protected SearchResults<Application> search() {
    UUID uUID = parseUUID(this.s.name);
    if (uUID != null) {
      ClientResponse<ApplicationResponse, Void> clientResponse = this.client.setTenantId(this.s.tenantId).retrieveApplication(uUID);
      if (clientResponse.wasSuccessful() && ((ApplicationResponse)clientResponse.getSuccessResponse()).application.state == ObjectState.Inactive)
        return new SearchResults<>(List.of(((ApplicationResponse)clientResponse.getSuccessResponse()).application), 1L); 
      return null;
    } 
    this.s.state = ObjectState.Inactive;
    ApplicationSearchResponse applicationSearchResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchApplications((new ApplicationSearchRequest(this.s)).with(())));
    return new SearchResults<>(applicationSearchResponse.applications, applicationSearchResponse.total);
  }
}
