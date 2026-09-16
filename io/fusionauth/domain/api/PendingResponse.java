package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.User;
import java.util.List;

public class PendingResponse {
  public List<User> users;
  
  @JacksonConstructor
  public PendingResponse() {}
  
  public PendingResponse(List<User> paramList) {
    this.users = paramList;
  }
}
