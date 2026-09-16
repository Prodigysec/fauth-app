package io.fusionauth.app.action.api.user.refreshToken;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.api.user.RefreshTokenImportRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class ImportAction extends BaseTenantAPIAction {
  @JSONRequest
  public final RefreshTokenImportRequest request = new RefreshTokenImportRequest();
  
  private final RefreshTokenService refreshTokenService;
  
  @Inject
  public ImportAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, RefreshTokenService paramRefreshTokenService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.refreshTokenService = paramRefreshTokenService;
  }
  
  public String post() {
    try {
      this.refreshTokenService.createBulk(getTenant(), this.request.refreshTokens);
    } catch (Exception exception) {
      this.frontEndSupport.addGeneralError("[RefreshTokenImportRequestFailed]", new Object[0]);
      EventLogHelper.create(new EventLog(EventLogType.Error, "Refresh Token Import request failed. This is likely a database UK violation.", exception));
      return "input";
    } 
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.request.refreshTokens == null || this.request.refreshTokens.isEmpty()) {
      this.frontEndSupport.addFieldError("refreshTokens", "[missing]refreshTokens", new Object[0]);
      return;
    } 
    Errors errors = this.refreshTokenService.validateBulkCreate(getTenant(), this.request.refreshTokens, this.request.validateDbConstraints);
    this.frontEndSupport.transfer(errors);
  }
}
