package io.fusionauth.domain.oauth2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.jwks.domain.JSONWebKey;
import java.util.List;

public class JWKSResponse implements Buildable<JWKSResponse> {
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public List<JSONWebKey> keys;
  
  @JacksonConstructor
  public JWKSResponse() {}
  
  public JWKSResponse(List<JSONWebKey> paramList) {
    this.keys = paramList;
  }
}
