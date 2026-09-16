package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumnable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Family implements Buildable<Family>, JSONColumnable {
  public final List<FamilyMember> members = new ArrayList<>();
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public Family(UUID paramUUID) {
    this.id = paramUUID;
  }
  
  public Family(UUID paramUUID, List<FamilyMember> paramList) {
    this.id = paramUUID;
    this.members.addAll(paramList);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Family))
      return false; 
    Family family = (Family)paramObject;
    return (Objects.equals(this.id, family.id) && 
      Objects.equals(this.insertInstant, family.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, family.lastUpdateInstant) && 
      Objects.equals(this.members, family.members));
  }
  
  public FamilyMember getMember(UUID paramUUID) {
    return this.members.stream()
      .filter(paramFamilyMember -> paramFamilyMember.userId.equals(paramUUID))
      .findFirst()
      .orElse(null);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.id, this.insertInstant, this.lastUpdateInstant, this.members });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  @JacksonConstructor
  public Family() {}
}
