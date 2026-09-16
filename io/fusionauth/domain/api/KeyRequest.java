package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Key;

public class KeyRequest {
  public Key key;
  
  @JacksonConstructor
  public KeyRequest() {}
  
  public KeyRequest(Key paramKey) {
    this.key = paramKey;
  }
}
