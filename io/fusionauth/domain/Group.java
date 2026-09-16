package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Group implements Buildable<Group>, Tenantable {
  public final Map<String, Object> data = new LinkedHashMap<>();
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public String name;
  
  public Map<UUID, List<ApplicationRole>> roles = new HashMap<>();
  
  public UUID tenantId;
  
  public Group(String paramString) {
    this.name = paramString;
  }
  
  public Group(Group paramGroup) {
    this.id = paramGroup.id;
    this.data.putAll(paramGroup.data);
    this.insertInstant = paramGroup.insertInstant;
    this.lastUpdateInstant = paramGroup.lastUpdateInstant;
    this.name = paramGroup.name;
    paramGroup.roles.forEach((paramUUID, paramList) -> this.roles.put(paramUUID, (List<ApplicationRole>)((List)paramGroup.roles.get(paramUUID)).stream().map(ApplicationRole::new).collect(Collectors.toList())));
    this.tenantId = paramGroup.tenantId;
  }
  
  public Group(UUID paramUUID, String paramString) {
    this.id = paramUUID;
    this.name = paramString;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Group))
      return false; 
    Group group = (Group)paramObject;
    return (Objects.equals(this.data, group.data) && 
      Objects.equals(this.id, group.id) && 
      Objects.equals(this.name, group.name) && 
      Objects.equals(this.roles, group.roles) && 
      Objects.equals(this.tenantId, group.tenantId) && 
      Objects.equals(this.insertInstant, group.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, group.lastUpdateInstant));
  }
  
  public UUID getTenantId() {
    return this.tenantId;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.data, this.id, this.name, this.roles, this.tenantId, this.insertInstant, this.lastUpdateInstant });
  }
  
  public Group sort() {
    for (List<ApplicationRole> list : this.roles.values())
      list.sort(Comparator.comparing(paramApplicationRole -> paramApplicationRole.name)); 
    return this;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  @JacksonConstructor
  public Group() {}
}
