package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseSearchTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.user.SearchRequest;
import io.fusionauth.domain.api.user.SearchResponse;
import io.fusionauth.domain.search.BaseElasticSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.SortField;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(value = "{queryString}", requiresAuthentication = true, scheme = {"api"})
public class SearchAction extends BaseSearchTenantAPIAction {
  private final UserReaderService userReader;
  
  private final UserService userService;
  
  public List<String> expand;
  
  @JSONRequest
  public SearchRequest request;
  
  @JSONResponse
  public SearchResponse response;
  
  @Inject
  public SearchAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.userReader = paramUserReaderService;
    this.userService = paramUserService;
  }
  
  public String put() {
    this.userService.refreshSearchIndex();
    return "success";
  }
  
  protected BaseElasticSearchCriteria criteria() {
    return (this.request != null) ? this.request.search : null;
  }
  
  protected String search() {
    Set<UserReaderService.UserExpansion> set = UserReaderService.UserExpansion.all();
    if (this.request != null)
      this.expand = this.request.expand; 
    if (this.expand != null)
      set = (Set<UserReaderService.UserExpansion>)this.expand.stream().map(UserReaderService.UserExpansion::safeValueOf).filter(Objects::nonNull).collect(Collectors.toSet()); 
    this.response = new SearchResponse();
    if (!set.contains(UserReaderService.UserExpansion.memberships))
      this.response.expandable.add(UserReaderService.UserExpansion.memberships.name()); 
    if (!set.contains(UserReaderService.UserExpansion.registrations))
      this.response.expandable.add(UserReaderService.UserExpansion.registrations.name()); 
    if (this.ids != null && !this.ids.isEmpty()) {
      this.ids = (new HashSet(this.ids)).stream().toList();
      this.response.users = this.userReader.retrieveByIds(getOptionalTenantId(), this.ids, set);
      this.response.total = this.response.users.size();
      this.response.totalEqualToActual = true;
    } else if (this.query != null) {
      SearchResults<User> searchResults = this.userReader.searchByQuery(getOptionalTenantId(), this.query, this.startRow, this.sortFields, this.numberOfResults, this.accurateTotal, set, this.lastSort, this.pointInTimeId);
      this.response.users = searchResults.results;
      this.response.total = searchResults.total;
      this.response.totalEqualToActual = searchResults.totalEqualToActual;
      this.response.nextResults = searchResults.nextResults;
    } else {
      SearchResults<User> searchResults = this.userReader.searchByQueryString(getOptionalTenantId(), this.queryString, this.numberOfResults, this.startRow, this.sortFields, this.accurateTotal, set, this.lastSort, this.pointInTimeId);
      this.response.users = searchResults.results;
      this.response.total = searchResults.total;
      this.response.totalEqualToActual = searchResults.totalEqualToActual;
      this.response.nextResults = searchResults.nextResults;
    } 
    if (this.response.users.size() > 0)
      this.response.users.forEach(User::secure); 
    return "render";
  }
  
  protected Errors validateSearchQuery(UUID paramUUID, String paramString1, String paramString2, List<SortField> paramList, int paramInt1, int paramInt2) {
    return this.userService.validateSearchQuery(paramUUID, paramString1, paramString2, paramList, paramInt1, paramInt2);
  }
}
