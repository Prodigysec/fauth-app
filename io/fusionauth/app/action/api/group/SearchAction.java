package io.fusionauth.app.action.api.group;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.group.GroupReaderService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.GroupSearchRequest;
import io.fusionauth.domain.api.GroupSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.GroupSearchCriteria;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api"})
public class SearchAction extends BaseSearchAPIAction<GroupSearchCriteria> {
  @JSONRequest
  public final GroupSearchRequest request = new GroupSearchRequest();
  
  private final GroupReaderService groupReader;
  
  @JSONResponse
  public GroupSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, GroupReaderService paramGroupReaderService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.groupReader = paramGroupReaderService;
  }
  
  public String name() {
    return (criteria()).name;
  }
  
  public void setName(String paramString) {
    (criteria()).name = paramString;
  }
  
  public void setTenantId(UUID paramUUID) {
    (criteria()).tenantId = paramUUID;
  }
  
  public UUID tenantId() {
    return (criteria()).tenantId;
  }
  
  protected GroupSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    if (tenantIdWasSpecified())
      (criteria()).tenantId = this.tenant.id; 
    this.response = new GroupSearchResponse(this.groupReader.search(this.request.search));
    return "render";
  }
}
