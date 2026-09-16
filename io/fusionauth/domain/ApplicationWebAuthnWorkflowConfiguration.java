package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;

public class ApplicationWebAuthnWorkflowConfiguration extends Enableable implements Buildable<ApplicationWebAuthnWorkflowConfiguration> {
  @JacksonConstructor
  public ApplicationWebAuthnWorkflowConfiguration() {}
  
  public ApplicationWebAuthnWorkflowConfiguration(ApplicationWebAuthnWorkflowConfiguration paramApplicationWebAuthnWorkflowConfiguration) {
    this.enabled = paramApplicationWebAuthnWorkflowConfiguration.enabled;
  }
  
  public boolean equals(Object paramObject) {
    return super.equals(paramObject);
  }
  
  public int hashCode() {
    return super.hashCode();
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
