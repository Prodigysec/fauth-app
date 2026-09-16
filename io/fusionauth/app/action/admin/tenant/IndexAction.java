package io.fusionauth.app.action.admin.tenant;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.TenantResponse;
import io.fusionauth.domain.api.TenantSearchRequest;
import io.fusionauth.domain.api.TenantSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.TenantSearchCriteria;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "tenant_manager"})
public class IndexAction extends BaseSearchAction<Tenant, TenantSearchCriteria> {
  @FTLVariable
  public UUID defaultTenantId;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @PostParameterMethod
  public void postParameter() {
    this.defaultTenantId = this.frontEndSupport.tenantReader.retrieveFusionAuthTenantId();
  }
  
  protected TenantSearchCriteria defaultSearchCriteria() {
    return new TenantSearchCriteria();
  }
  
  protected SearchResults<Tenant> search() {
    UUID uUID = parseUUID(this.s.name);
    if (uUID != null) {
      ClientResponse<TenantResponse, Errors> clientResponse = this.client.retrieveTenant(uUID);
      if (clientResponse.wasSuccessful())
        return new SearchResults<>(List.of(((TenantResponse)clientResponse.getSuccessResponse()).tenant), 1L); 
      return null;
    } 
    TenantSearchResponse tenantSearchResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchTenants(new TenantSearchRequest(this.s)));
    return new SearchResults<>(tenantSearchResponse.tenants, tenantSearchResponse.total);
  }
}
