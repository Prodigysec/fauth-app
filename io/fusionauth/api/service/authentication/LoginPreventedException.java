package io.fusionauth.api.service.authentication;

import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.api.LoginPreventedResponse;
import java.util.Collections;
import java.util.List;
import org.primeframework.mvc.ErrorException;

public class LoginPreventedException extends ErrorException {
  public List<LoginPreventedResponse> actions;
  
  public LoginPreventedException(UserActionLog paramUserActionLog) {
    super("login-prevented");
    this.actions = Collections.singletonList(new LoginPreventedResponse(paramUserActionLog));
  }
  
  public LoginPreventedException(List<LoginPreventedResponse> paramList) {
    super("login-prevented");
    this.actions = paramList;
  }
}
