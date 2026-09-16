package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.UserDeleteRequest;
import io.fusionauth.domain.api.UserDeleteResponse;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class BulkAction extends BaseTenantAPIAction {
  @JSONRequest(httpMethods = {"DELETE"})
  public final UserDeleteRequest request = new UserDeleteRequest();
  
  private final UserService userService;
  
  @JSONResponse
  public UserDeleteResponse response;
  
  @Inject
  public BulkAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.userService = paramUserService;
  }
  
  public String delete() {
    this.response = new UserDeleteResponse();
    this.response.hardDelete = this.request.hardDelete;
    this.response.dryRun = this.request.dryRun;
    if (this.request.userIds != null && !this.request.userIds.isEmpty()) {
      if (this.request.hardDelete) {
        this.response.userIds = this.userService.deleteAllByIds(getOptionalTenantId(), this.request.userIds, this.request.eventInfo, this.request.dryRun);
      } else {
        this.response.userIds = this.userService.deactivateAllByIds(getOptionalTenantId(), this.request.userIds, this.request.eventInfo, this.request.dryRun);
      } 
    } else if (this.request.query != null) {
      this.response.userIds = this.userService.deleteAllBySearchQuery(getOptionalTenantId(), this.request.query, this.request.hardDelete, this.request.dryRun, this.request.limit, this.request.eventInfo);
    } else {
      this.response.userIds = this.userService.deleteAllBySearchQueryString(getOptionalTenantId(), this.request.queryString, this.request.hardDelete, this.request.dryRun, this.request.limit, this.request.eventInfo);
    } 
    this.response.total = this.response.userIds.size();
    return "render";
  }
  
  public void setDryRun(boolean paramBoolean) {
    this.request.dryRun = paramBoolean;
  }
  
  public void setHardDelete(boolean paramBoolean) {
    this.request.hardDelete = paramBoolean;
  }
  
  public void setLimit(int paramInt) {
    this.request.limit = paramInt;
  }
  
  public void setQuery(String paramString) {
    this.request.query = paramString;
  }
  
  public void setQueryString(String paramString) {
    this.request.queryString = paramString;
  }
  
  public void setUserId(List<UUID> paramList) {
    this.request.userIds = paramList;
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if ((this.request.userIds == null || this.request.userIds.isEmpty()) && this.request.query == null && this.request.queryString == null) {
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]);
    } else {
      UserService.ValidationResult validationResult = this.userService.validateBulkDelete(getOptionalTenant(), this.request.userIds, this.request.limit, this.request.query, this.request.queryString);
      this.frontEndSupport.transfer(validationResult.errors);
    } 
  }
}
