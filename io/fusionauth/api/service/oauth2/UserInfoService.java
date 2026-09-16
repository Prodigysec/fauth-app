package io.fusionauth.api.service.oauth2;

import io.fusionauth.domain.Application;
import io.fusionauth.domain.User;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.jwt.domain.JWT;
import java.util.Map;

public interface UserInfoService {
  Map<String, Object> retrieveUserInfo(JWT paramJWT, User paramUser, Application paramApplication);
  
  UserInfoValidationResult validateRetrieveUserInfo(String paramString);
  
  public static class UserInfoValidationResult {
    public Application application;
    
    public OAuthError error;
    
    public JWT jwt;
    
    public User user;
  }
}
