package io.fusionauth.domain.api.cache;

import com.inversoft.json.JacksonConstructor;
import java.util.Collections;
import java.util.List;

public class ReloadRequest {
  public List<String> names;
  
  @JacksonConstructor
  public ReloadRequest() {}
  
  public ReloadRequest(List<String> paramList) {
    this.names = paramList;
  }
  
  public ReloadRequest(String paramString) {
    this.names = Collections.singletonList(paramString);
  }
}
