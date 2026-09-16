package io.fusionauth.app.action.tenantManager.ajax.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.action.tenantManager.BaseTenantManagerAction;
import io.fusionauth.app.domain.Pagination;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.user.SearchRequest;
import io.fusionauth.domain.api.user.SearchResponse;
import io.fusionauth.domain.search.UserSearchCriteria;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Action(requiresAuthentication = true, scheme = {"tenant-manager"})
@Forward(code = "input", page = "/tenant-manager/ajax/user/search.ftl")
public class SearchAction extends BaseTenantManagerAction {
  public int currentPage;
  
  public long firstResult;
  
  public long lastResult;
  
  public int numberOfPages;
  
  public UserSearchCriteria s = new UserSearchCriteria();
  
  public long total;
  
  public List<User> users = List.of();
  
  @Inject
  public SearchAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  public String get() {
    performSearch();
    return "input";
  }
  
  private void calculatePagination() {
    Pagination pagination = new Pagination(this.s.startRow, this.s.numberOfResults, (this.users == null) ? 0 : this.users.size(), this.total);
    this.currentPage = pagination.currentPage;
    this.numberOfPages = pagination.numberOfPages;
    this.firstResult = pagination.firstResult;
    this.lastResult = pagination.lastResult;
  }
  
  private UUID parseUUID(String paramString) {
    try {
      return UUID.fromString(paramString);
    } catch (IllegalArgumentException illegalArgumentException) {
      return null;
    } 
  }
  
  private void performSearch() {
    if (this.client == null)
      return; 
    if (this.s.queryString == null) {
      this.s.queryString = "";
    } else {
      this.s.queryString = this.s.queryString.trim();
    } 
    UUID uUID = parseUUID(this.s.queryString);
    if (uUID != null) {
      ClientResponse<SearchResponse, Errors> clientResponse = this.client.searchUsersByIds(Collections.singletonList(uUID));
      if (clientResponse.wasSuccessful()) {
        this.users = ((SearchResponse)clientResponse.successResponse).users;
        this.total = ((SearchResponse)clientResponse.successResponse).total;
      } 
    } else {
      if (this.s.queryString.isEmpty() || this.s.queryString.isBlank())
        this.s.queryString = "*"; 
      this.s.accurateTotal = true;
      ClientResponse<SearchResponse, Errors> clientResponse = this.client.searchUsersByQuery(new SearchRequest(this.s));
      if (clientResponse.wasSuccessful()) {
        this.users = (((SearchResponse)clientResponse.successResponse).users != null) ? ((SearchResponse)clientResponse.successResponse).users : List.of();
        this.total = ((SearchResponse)clientResponse.successResponse).total;
      } 
    } 
    calculatePagination();
  }
}
