package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.GroupMember;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MemberResponse {
  public Map<UUID, List<GroupMember>> members;
  
  @JacksonConstructor
  public MemberResponse() {}
  
  public MemberResponse(Map<UUID, List<GroupMember>> paramMap) {
    this.members = paramMap;
  }
}
