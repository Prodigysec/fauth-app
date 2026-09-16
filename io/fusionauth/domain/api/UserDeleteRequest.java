package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import java.util.List;
import java.util.UUID;

public class UserDeleteRequest extends BaseEventRequest implements Buildable<UserDeleteRequest> {
  public boolean dryRun;
  
  public boolean hardDelete;
  
  public int limit = 10000;
  
  public String query;
  
  public String queryString;
  
  public List<UUID> userIds;
  
  @JacksonConstructor
  public UserDeleteRequest() {}
  
  public UserDeleteRequest(List<UUID> paramList) {
    this.userIds = paramList;
  }
  
  public UserDeleteRequest(List<UUID> paramList, boolean paramBoolean) {
    this.hardDelete = paramBoolean;
    this.userIds = paramList;
  }
  
  public UserDeleteRequest(EventInfo paramEventInfo, List<UUID> paramList, boolean paramBoolean) {
    super(paramEventInfo);
    this.hardDelete = paramBoolean;
    this.userIds = paramList;
  }
}
