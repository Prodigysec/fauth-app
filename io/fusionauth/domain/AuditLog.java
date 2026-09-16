package io.fusionauth.domain;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.util.Normalizer;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AuditLog implements Buildable<AuditLog>, JSONColumnable {
  public final Map<String, Object> data = new LinkedHashMap<>();
  
  public Long id;
  
  public ZonedDateTime insertInstant;
  
  public String insertUser;
  
  public String message;
  
  @JSONColumn
  public Object newValue;
  
  @JSONColumn
  public Object oldValue;
  
  @JSONColumn
  public String reason;
  
  public UUID tenantId;
  
  public AuditLog() {}
  
  public AuditLog(String paramString1, String paramString2) {
    this.insertUser = paramString1;
    this.message = paramString2;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof AuditLog))
      return false; 
    AuditLog auditLog = (AuditLog)paramObject;
    return (Objects.equals(this.data, auditLog.data) && 
      Objects.equals(this.id, auditLog.id) && 
      Objects.equals(this.insertInstant, auditLog.insertInstant) && 
      Objects.equals(this.insertUser, auditLog.insertUser) && 
      Objects.equals(this.message, auditLog.message) && 
      Objects.equals(this.newValue, auditLog.newValue) && 
      Objects.equals(this.oldValue, auditLog.oldValue) && 
      Objects.equals(this.reason, auditLog.reason) && 
      Objects.equals(this.tenantId, auditLog.tenantId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.data, this.id, this.insertInstant, this.insertUser, this.message, this.newValue, this.oldValue, this.reason, this.tenantId });
  }
  
  public void normalize() {
    this.insertUser = Normalizer.trim(this.insertUser);
    this.message = Normalizer.trim(this.message);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
