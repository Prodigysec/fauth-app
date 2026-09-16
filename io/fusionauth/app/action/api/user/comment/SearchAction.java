package io.fusionauth.app.action.api.user.comment;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.comment.UserCommentService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.UserCommentSearchRequest;
import io.fusionauth.domain.api.UserCommentSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.UserCommentSearchCriteria;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api"})
public class SearchAction extends BaseSearchAPIAction<UserCommentSearchCriteria> {
  private final UserCommentService userCommentService;
  
  @JSONRequest
  public UserCommentSearchRequest request = new UserCommentSearchRequest();
  
  @JSONResponse
  public UserCommentSearchResponse response = new UserCommentSearchResponse();
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, UserCommentService paramUserCommentService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.userCommentService = paramUserCommentService;
  }
  
  public String comment() {
    return (criteria()).comment;
  }
  
  public UUID commenterId() {
    return (criteria()).commenterId;
  }
  
  public void setComment(String paramString) {
    (criteria()).comment = paramString;
  }
  
  public void setCommenterId(UUID paramUUID) {
    (criteria()).commenterId = paramUUID;
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
  
  protected UserCommentSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    if (tenantIdWasSpecified())
      (criteria()).tenantId = this.tenant.id; 
    this.response = new UserCommentSearchResponse(this.userCommentService.search(this.request.search));
    return "render";
  }
}
