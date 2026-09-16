package io.fusionauth.app.action.api.system.loginRecord;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.login.LoginService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.DisplayableRawLogin;
import io.fusionauth.domain.api.LoginRecordSearchRequest;
import io.fusionauth.domain.api.LoginRecordSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.LoginRecordSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class SearchAction extends BaseSearchAPIAction<LoginRecordSearchCriteria> {
  @JSONRequest
  public final LoginRecordSearchRequest request = new LoginRecordSearchRequest();
  
  private final LoginService loginService;
  
  @JSONResponse
  public LoginRecordSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, LoginService paramLoginService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.loginService = paramLoginService;
  }
  
  public UUID getApplicationId() {
    return (criteria()).applicationId;
  }
  
  public void setApplicationId(UUID paramUUID) {
    (criteria()).applicationId = paramUUID;
  }
  
  public ZonedDateTime getEnd() {
    return (criteria()).end;
  }
  
  public void setEnd(ZonedDateTime paramZonedDateTime) {
    (criteria()).end = paramZonedDateTime;
  }
  
  public ZonedDateTime getStart() {
    return (criteria()).start;
  }
  
  public void setStart(ZonedDateTime paramZonedDateTime) {
    (criteria()).start = paramZonedDateTime;
  }
  
  public UUID getUserId() {
    return (criteria()).userId;
  }
  
  public void setUserId(UUID paramUUID) {
    (criteria()).userId = paramUUID;
  }
  
  public boolean isRetrieveTotal() {
    return this.request.retrieveTotal;
  }
  
  public void setRetrieveTotal(boolean paramBoolean) {
    this.request.retrieveTotal = paramBoolean;
  }
  
  protected LoginRecordSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    SearchResults<DisplayableRawLogin> searchResults = new SearchResults();
    searchResults.results = this.loginService.retrieveRawLoginsByCriteria(this.request.search);
    if (this.request.retrieveTotal) {
      searchResults.total = this.loginService.retrieveRawLoginsByCriteriaCount(this.request.search);
    } else {
      searchResults.total = -1L;
    } 
    this.response = new LoginRecordSearchResponse(searchResults);
    return "render";
  }
}
