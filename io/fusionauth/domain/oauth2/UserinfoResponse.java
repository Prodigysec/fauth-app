package io.fusionauth.domain.oauth2;

import com.inversoft.json.JacksonConstructor;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserinfoResponse extends LinkedHashMap<String, Object> {
  @JacksonConstructor
  public UserinfoResponse() {}
  
  public UserinfoResponse(Map<String, Object> paramMap) {
    putAll(paramMap);
  }
}
