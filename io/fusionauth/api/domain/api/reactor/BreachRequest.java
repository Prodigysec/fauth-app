package io.fusionauth.api.domain.api.reactor;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import java.util.ArrayList;
import java.util.List;

public class BreachRequest implements Buildable<BreachRequest> {
  public String licenseId;
  
  public List<String> loginIds = new ArrayList<>();
  
  public String password;
  
  public BreachRequest(List<String> paramList, String paramString) {
    this.loginIds.addAll(paramList);
    this.password = paramString;
  }
  
  @JacksonConstructor
  public BreachRequest() {}
}
