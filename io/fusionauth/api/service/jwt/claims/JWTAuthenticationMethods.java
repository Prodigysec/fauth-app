package io.fusionauth.api.service.jwt.claims;

import io.fusionauth.jwt.domain.JWT;
import java.util.List;

public class JWTAuthenticationMethods {
  public static final String Claim = "amr";
  
  public static final String Email = "email";
  
  public static final String Federated = "fed";
  
  public static final String MultiFactor = "mfa";
  
  public static final String None = "none";
  
  public static final String OneTimePassword = "otp";
  
  public static final String Password = "pwd";
  
  public static final String SMS = "sms";
  
  public static final String TimeBasedOneTimePassword = "totp";
  
  public static final String WebAuthn = "pop";
  
  public static boolean contains(JWT paramJWT, String paramString) {
    if (paramJWT == null)
      return false; 
    List list = paramJWT.getList("amr");
    if (list == null)
      return false; 
    return list.contains(paramString);
  }
}
