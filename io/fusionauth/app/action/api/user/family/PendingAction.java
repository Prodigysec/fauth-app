package io.fusionauth.app.action.api.user.family;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.PendingResponse;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class PendingAction extends BaseTenantAPIAction {
  private final UserReaderService userReader;
  
  public String parentEmail;
  
  @JSONResponse
  public PendingResponse response;
  
  @Inject
  public PendingAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, UserReaderService paramUserReaderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.userReader = paramUserReaderService;
  }
  
  public String get() {
    if (this.parentEmail != null) {
      List<User> list = this.userReader.retrieveByParentEmail((getTenant()).id, this.parentEmail);
      list.sort(Comparator.comparing(paramUser -> paramUser.id));
      list.forEach(User::secure);
      this.response = new PendingResponse(list);
    } 
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.parentEmail == null)
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]); 
  }
}
