package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import java.util.Objects;
import java.util.UUID;

public class ApplicationFormConfiguration implements Buildable<ApplicationFormConfiguration> {
  @ExcludeFromJSONColumn
  public UUID adminRegistrationFormId;
  
  public SelfServiceFormConfiguration selfServiceFormConfiguration = new SelfServiceFormConfiguration();
  
  @ExcludeFromJSONColumn
  public UUID selfServiceFormId;
  
  @JacksonConstructor
  public ApplicationFormConfiguration() {}
  
  public ApplicationFormConfiguration(ApplicationFormConfiguration paramApplicationFormConfiguration) {
    this.adminRegistrationFormId = paramApplicationFormConfiguration.adminRegistrationFormId;
    this.selfServiceFormConfiguration = new SelfServiceFormConfiguration(paramApplicationFormConfiguration.selfServiceFormConfiguration);
    this.selfServiceFormId = paramApplicationFormConfiguration.selfServiceFormId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    ApplicationFormConfiguration applicationFormConfiguration = (ApplicationFormConfiguration)paramObject;
    return (Objects.equals(this.adminRegistrationFormId, applicationFormConfiguration.adminRegistrationFormId) && 
      Objects.equals(this.selfServiceFormId, applicationFormConfiguration.selfServiceFormId) && 
      Objects.equals(this.selfServiceFormConfiguration, applicationFormConfiguration.selfServiceFormConfiguration));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.adminRegistrationFormId, this.selfServiceFormId, this.selfServiceFormConfiguration });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
