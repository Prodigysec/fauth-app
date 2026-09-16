package io.fusionauth.app.action.ajax.tenant;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.TenantSearchRequest;
import io.fusionauth.domain.api.TenantSearchResponse;
import io.fusionauth.domain.search.TenantSearchCriteria;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, constraints = {"admin", "tenant_manager", "system_manager"})
public class SearchAction extends BaseAJAXAction {
  public String queryString;
  
  @JSONResponse
  public TenantSearchResponse response;
  
  @Inject
  protected SearchAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.response = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchTenants(new TenantSearchRequest((new TenantSearchCriteria()).with(()).with(()).with(()))));
    return "render-json";
  }
}
