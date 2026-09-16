package io.fusionauth.api.service.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.jwks.domain.JSONWebKey;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.jwt.domain.Header;
import io.fusionauth.jwt.domain.JWT;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DPoPUtil {
  @Nullable
  public static JSONWebKey buildJSONWebKey(@Nonnull Header paramHeader) {
    try {
      Object object = paramHeader.get("jwk");
      ObjectMapper objectMapper = new ObjectMapper();
      String str = objectMapper.writeValueAsString(object);
      return (JSONWebKey)objectMapper.readValue(str, JSONWebKey.class);
    } catch (JsonProcessingException jsonProcessingException) {
      return null;
    } 
  }
  
  @Nullable
  public static String thumbprint(@Nonnull JWT paramJWT) {
    JSONWebKey jSONWebKey = buildJSONWebKey(paramJWT.header);
    if (jSONWebKey == null)
      return null; 
    return JWTUtils.generateJWS_kid_S256(jSONWebKey);
  }
}
