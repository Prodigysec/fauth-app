package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import java.util.Objects;
import java.util.UUID;

public class ApplicationAccessControlConfiguration implements Buildable<ApplicationAccessControlConfiguration> {
  @ExcludeFromJSONColumn
  public UUID uiIPAccessControlListId;
  
  @JacksonConstructor
  public ApplicationAccessControlConfiguration() {}
  
  public ApplicationAccessControlConfiguration(ApplicationAccessControlConfiguration paramApplicationAccessControlConfiguration) {
    this.uiIPAccessControlListId = paramApplicationAccessControlConfiguration.uiIPAccessControlListId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    ApplicationAccessControlConfiguration applicationAccessControlConfiguration = (ApplicationAccessControlConfiguration)paramObject;
    return Objects.equals(this.uiIPAccessControlListId, applicationAccessControlConfiguration.uiIPAccessControlListId);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.uiIPAccessControlListId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
