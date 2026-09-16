package io.fusionauth.api.service.authentication;

import java.util.UUID;

public interface OAuth1IdentityProviderAuthenticationService {
  String authenticateURI();
  
  RequestToken requestRequestToken(String paramString1, String paramString2, UUID paramUUID);
  
  public static class RequestToken {
    public String oauth_token;
    
    public String oauth_token_secret;
    
    public RequestToken(String param1String1, String param1String2) {
      this.oauth_token = param1String1;
      this.oauth_token_secret = param1String2;
    }
  }
}
