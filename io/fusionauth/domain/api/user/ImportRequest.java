package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.BaseEventRequest;
import java.util.List;

public class ImportRequest extends BaseEventRequest {
  public String encryptionScheme;
  
  public Integer factor;
  
  public List<User> users;
  
  public boolean validateDbConstraints;
  
  @JacksonConstructor
  public ImportRequest() {}
  
  public ImportRequest(List<User> paramList) {
    this.users = paramList;
  }
  
  public ImportRequest(EventInfo paramEventInfo, List<User> paramList) {
    super(paramEventInfo);
    this.users = paramList;
  }
}
