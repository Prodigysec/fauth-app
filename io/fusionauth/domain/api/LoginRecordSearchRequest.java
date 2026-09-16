package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.LoginRecordSearchCriteria;

public class LoginRecordSearchRequest {
  public boolean retrieveTotal;
  
  public LoginRecordSearchCriteria search = new LoginRecordSearchCriteria();
  
  @JacksonConstructor
  public LoginRecordSearchRequest() {}
  
  public LoginRecordSearchRequest(LoginRecordSearchCriteria paramLoginRecordSearchCriteria) {
    this.search = paramLoginRecordSearchCriteria;
  }
}
