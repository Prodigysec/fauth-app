package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class IPAccessControlList implements Buildable<IPAccessControlList>, JSONColumnable {
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public final Map<String, Object> data = new LinkedHashMap<>();
  
  @JSONColumn
  public List<IPAccessControlEntry> entries = new ArrayList<>();
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public String name;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    IPAccessControlList iPAccessControlList = (IPAccessControlList)paramObject;
    return (Objects.equals(this.data, iPAccessControlList.data) && 
      Objects.equals(this.entries, iPAccessControlList.entries) && 
      Objects.equals(this.id, iPAccessControlList.id) && 
      Objects.equals(this.insertInstant, iPAccessControlList.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, iPAccessControlList.lastUpdateInstant) && 
      Objects.equals(this.name, iPAccessControlList.name));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.data, this.entries, this.id, this.insertInstant, this.lastUpdateInstant, this.name });
  }
  
  public IPAccessControlList normalize() {
    boolean bool = false;
    Iterator<IPAccessControlEntry> iterator = this.entries.iterator();
    while (iterator.hasNext()) {
      IPAccessControlEntry iPAccessControlEntry = iterator.next();
      if ("*".equals(iPAccessControlEntry.startIPAddress)) {
        if (bool) {
          iterator.remove();
          continue;
        } 
        iPAccessControlEntry.endIPAddress = null;
        bool = true;
      } 
    } 
    this.entries.sort(Comparator.comparing(paramIPAccessControlEntry -> paramIPAccessControlEntry.startIPAddress, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(paramIPAccessControlEntry -> paramIPAccessControlEntry.endIPAddress, Comparator.nullsLast(Comparator.naturalOrder())));
    return this;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
