package io.fusionauth.domain.oauth2;

import com.inversoft.json.JacksonConstructor;
import java.util.LinkedHashMap;

public class IntrospectResponse extends LinkedHashMap<String, Object> {
  @JacksonConstructor
  public IntrospectResponse() {
    put("active", Boolean.valueOf(false));
  }
  
  public IntrospectResponse(boolean paramBoolean) {
    put("active", Boolean.valueOf(paramBoolean));
  }
}
