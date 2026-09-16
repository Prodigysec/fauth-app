package io.fusionauth.app.action.api.identityProvider;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.identity.IdentityProviderReaderService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.IdentityProviderSearchRequest;
import io.fusionauth.domain.api.IdentityProviderSearchResponse;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.IdentityProviderSearchCriteria;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api"})
public class SearchAction extends BaseSearchAPIAction<IdentityProviderSearchCriteria> {
  @JSONRequest
  public final IdentityProviderSearchRequest request = new IdentityProviderSearchRequest();
  
  private final IdentityProviderReaderService identityProviderReader;
  
  @JSONResponse
  public IdentityProviderSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, IdentityProviderReaderService paramIdentityProviderReaderService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.identityProviderReader = paramIdentityProviderReaderService;
  }
  
  public UUID getApplicationId() {
    return (criteria()).applicationId;
  }
  
  public void setApplicationId(UUID paramUUID) {
    (criteria()).applicationId = paramUUID;
  }
  
  public String getName() {
    return (criteria()).name;
  }
  
  public void setName(String paramString) {
    (criteria()).name = paramString;
  }
  
  public String getSource() {
    return (criteria()).source;
  }
  
  public void setSource(String paramString) {
    (criteria()).source = paramString;
  }
  
  public UUID getTenantId() {
    return (criteria()).tenantId;
  }
  
  public void setTenantId(UUID paramUUID) {
    (criteria()).tenantId = paramUUID;
  }
  
  public IdentityProviderType getType() {
    return (criteria()).type;
  }
  
  public void setType(IdentityProviderType paramIdentityProviderType) {
    (criteria()).type = paramIdentityProviderType;
  }
  
  protected IdentityProviderSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    if (tenantIdWasSpecified())
      (criteria()).tenantId = this.tenant.id; 
    this.response = new IdentityProviderSearchResponse(this.identityProviderReader.search(this.request.search));
    return "render";
  }
}
