package io.fusionauth.api.service.jwt;

import io.fusionauth.domain.Key;
import io.fusionauth.jwt.Signer;

public class JWTHelper {
  public static Signer buildSigner(Key paramKey) {
    switch (paramKey.algorithm) {
      default:
        throw new MatchException(null, null);
      case ES256:
      
      case ES384:
      
      case ES512:
      
      case HS256:
      
      case HS384:
      
      case HS512:
      
      case RS256:
      
      case RS384:
      
      case RS512:
      
      case Ed25519:
      
      case None:
        break;
    } 
    return 









      
      null;
  }
}
