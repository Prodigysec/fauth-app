package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MemberDeleteRequest {
  public List<UUID> memberIds;
  
  public Map<UUID, List<UUID>> members;
  
  @JacksonConstructor
  public MemberDeleteRequest() {}
  
  public MemberDeleteRequest(List<UUID> paramList) {
    this.memberIds = paramList;
  }
  
  public MemberDeleteRequest(Map<UUID, List<UUID>> paramMap) {
    this.members = paramMap;
  }
}
