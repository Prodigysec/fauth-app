package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Key;
import java.util.List;

public class KeyResponse {
  public Key key;
  
  public List<Key> keys;
  
  @JacksonConstructor
  public KeyResponse() {}
  
  public KeyResponse(Key paramKey) {
    this.key = paramKey;
  }
  
  public KeyResponse(List<Key> paramList) {
    this.keys = paramList;
  }
}
