package io.fusionauth.app.service.legacy;

import com.google.inject.Inject;
import io.fusionauth.api.service.jwt.JWTHelper;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.service.system.KeyService;
import io.fusionauth.domain.Key;
import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.domain.Header;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import java.util.UUID;

public class DefaultLegacyTokenSigner implements LegacyTokenSigner {
  private final KeyReaderService keyReader;
  
  @Inject
  public DefaultLegacyTokenSigner(KeyReaderService paramKeyReaderService) {
    this.keyReader = paramKeyReaderService;
  }
  
  public String sign(JWT paramJWT, String paramString1, UUID paramUUID, String paramString2, boolean paramBoolean) {
    Key key;
    if (paramString1 == null)
      return null; 
    if (paramBoolean && paramUUID != null && paramString1.equals(paramUUID.toString())) {
      HMACSigner hMACSigner = HMACSigner.newSHA256Signer(paramString2, paramString1);
      return JWT.getEncoder().encode(paramJWT, (Signer)hMACSigner, paramHeader -> paramHeader.set("kid", paramSigner.getKid()));
    } 
    try {
      key = this.keyReader.retrieveById(UUID.fromString(paramString1));
    } catch (IllegalArgumentException illegalArgumentException) {
      key = this.keyReader.retrieveByKid(paramString1);
    } 
    if (key == null)
      return null; 
    if (paramBoolean && KeyService.ClientSecretShadowKeys.contains(key.id)) {
      key = new Key(key);
      key.secret = paramString2;
      key.kid = paramUUID.toString();
    } 
    Signer signer = JWTHelper.buildSigner(key);
    if (signer == null)
      return null; 
    return JWT.getEncoder().encode(paramJWT, signer, paramHeader -> paramHeader.set("kid", paramSigner.getKid()));
  }
}
