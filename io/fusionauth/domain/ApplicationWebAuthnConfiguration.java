package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class ApplicationWebAuthnConfiguration extends Enableable implements Buildable<ApplicationWebAuthnConfiguration> {
  public ApplicationWebAuthnWorkflowConfiguration bootstrapWorkflow = new ApplicationWebAuthnWorkflowConfiguration();
  
  public ApplicationWebAuthnWorkflowConfiguration reauthenticationWorkflow = new ApplicationWebAuthnWorkflowConfiguration();
  
  @JacksonConstructor
  public ApplicationWebAuthnConfiguration() {}
  
  public ApplicationWebAuthnConfiguration(ApplicationWebAuthnConfiguration paramApplicationWebAuthnConfiguration) {
    this.bootstrapWorkflow = new ApplicationWebAuthnWorkflowConfiguration(paramApplicationWebAuthnConfiguration.bootstrapWorkflow);
    this.enabled = paramApplicationWebAuthnConfiguration.enabled;
    this.reauthenticationWorkflow = new ApplicationWebAuthnWorkflowConfiguration(paramApplicationWebAuthnConfiguration.reauthenticationWorkflow);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    ApplicationWebAuthnConfiguration applicationWebAuthnConfiguration = (ApplicationWebAuthnConfiguration)paramObject;
    return (Objects.equals(this.bootstrapWorkflow, applicationWebAuthnConfiguration.bootstrapWorkflow) && Objects.equals(this.reauthenticationWorkflow, applicationWebAuthnConfiguration.reauthenticationWorkflow));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.bootstrapWorkflow, this.reauthenticationWorkflow });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
