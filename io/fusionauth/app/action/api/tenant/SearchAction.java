package io.fusionauth.app.action.api.tenant;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.TenantSearchRequest;
import io.fusionauth.domain.api.TenantSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.TenantSearchCriteria;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class SearchAction extends BaseSearchAPIAction<TenantSearchCriteria> {
  @JSONRequest
  public final TenantSearchRequest request = new TenantSearchRequest();
  
  private final TenantReaderService tenantReader;
  
  @JSONResponse
  public TenantSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, TenantReaderService paramTenantReaderService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.tenantReader = paramTenantReaderService;
  }
  
  public String name() {
    return (criteria()).name;
  }
  
  public void setName(String paramString) {
    (criteria()).name = paramString;
  }
  
  protected TenantSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    this.response = new TenantSearchResponse(this.tenantReader.search(this.request.search));
    this.response.tenants.forEach(Tenant::secure);
    return "render";
  }
}
