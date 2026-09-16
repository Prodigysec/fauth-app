package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.GroupMember;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MemberRequest implements Buildable<MemberRequest> {
  public Map<UUID, List<GroupMember>> members;
  
  @JacksonConstructor
  public MemberRequest() {
    this.members = new LinkedHashMap<>();
  }
  
  public MemberRequest(Map<UUID, List<GroupMember>> paramMap) {
    this.members = paramMap;
  }
  
  public MemberRequest(UUID paramUUID, List<GroupMember> paramList) {
    this.members = new HashMap<>(1);
    this.members.put(paramUUID, paramList);
  }
}
