package io.fusionauth.app.action.api.group.member;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.group.GroupReaderService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.GroupMemberSearchRequest;
import io.fusionauth.domain.api.GroupMemberSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.GroupMemberSearchCriteria;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api"})
public class SearchAction extends BaseSearchAPIAction<GroupMemberSearchCriteria> {
  @JSONRequest
  public final GroupMemberSearchRequest request = new GroupMemberSearchRequest();
  
  private final GroupReaderService groupReader;
  
  @JSONResponse
  public GroupMemberSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, GroupReaderService paramGroupReaderService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.groupReader = paramGroupReaderService;
  }
  
  public UUID groupId() {
    return (criteria()).groupId;
  }
  
  public void setGroupId(UUID paramUUID) {
    (criteria()).groupId = paramUUID;
  }
  
  public void setTenantId(UUID paramUUID) {
    (criteria()).tenantId = paramUUID;
  }
  
  public void setUserId(UUID paramUUID) {
    (criteria()).userId = paramUUID;
  }
  
  public UUID tenantId() {
    return (criteria()).tenantId;
  }
  
  public UUID userId() {
    return (criteria()).userId;
  }
  
  protected GroupMemberSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    if (tenantIdWasSpecified())
      (criteria()).tenantId = this.tenant.id; 
    this.response = new GroupMemberSearchResponse(this.groupReader.search(this.request.search));
    return "render";
  }
}
