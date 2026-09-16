package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import io.fusionauth.api.service.login.LoginService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.user.RecentLoginResponse;
import io.fusionauth.domain.search.LoginRecordSearchCriteria;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(scheme = {"api-no-tenant"}, requiresAuthentication = true)
public class RecentLoginAction extends BaseAPIAction {
  private final LoginService loginService;
  
  private final UserReaderService userReader;
  
  public int limit = 10;
  
  public int offset;
  
  @JSONResponse
  public RecentLoginResponse response = new RecentLoginResponse();
  
  public UUID userId;
  
  @Inject
  public RecentLoginAction(FrontEndSupport paramFrontEndSupport, LoginService paramLoginService, UserReaderService paramUserReaderService) {
    super(paramFrontEndSupport);
    this.loginService = paramLoginService;
    this.userReader = paramUserReaderService;
  }
  
  public String get() {
    if (this.userId != null) {
      User user = this.userReader.retrieveById(null, this.userId);
      if (user == null)
        return "missing"; 
    } 
    this.response.logins = this.loginService.retrieveRawLoginsByCriteria((new LoginRecordSearchCriteria()).with(paramLoginRecordSearchCriteria -> paramLoginRecordSearchCriteria.userId = this.userId)
        .with(paramLoginRecordSearchCriteria -> paramLoginRecordSearchCriteria.startRow = this.offset)
        .with(paramLoginRecordSearchCriteria -> paramLoginRecordSearchCriteria.numberOfResults = this.limit));
    return "render";
  }
}
