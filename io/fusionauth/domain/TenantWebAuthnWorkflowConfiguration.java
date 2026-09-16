package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.webauthn.AuthenticatorAttachmentPreference;
import io.fusionauth.domain.webauthn.UserVerificationRequirement;
import java.util.Objects;

public class TenantWebAuthnWorkflowConfiguration extends Enableable implements Buildable<TenantWebAuthnWorkflowConfiguration> {
  public AuthenticatorAttachmentPreference authenticatorAttachmentPreference;
  
  public UserVerificationRequirement userVerificationRequirement;
  
  @JacksonConstructor
  public TenantWebAuthnWorkflowConfiguration() {}
  
  public TenantWebAuthnWorkflowConfiguration(TenantWebAuthnWorkflowConfiguration paramTenantWebAuthnWorkflowConfiguration) {
    this.enabled = paramTenantWebAuthnWorkflowConfiguration.enabled;
    this.authenticatorAttachmentPreference = paramTenantWebAuthnWorkflowConfiguration.authenticatorAttachmentPreference;
    this.userVerificationRequirement = paramTenantWebAuthnWorkflowConfiguration.userVerificationRequirement;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TenantWebAuthnWorkflowConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    TenantWebAuthnWorkflowConfiguration tenantWebAuthnWorkflowConfiguration = (TenantWebAuthnWorkflowConfiguration)paramObject;
    return (this.authenticatorAttachmentPreference == tenantWebAuthnWorkflowConfiguration.authenticatorAttachmentPreference && this.userVerificationRequirement == tenantWebAuthnWorkflowConfiguration.userVerificationRequirement);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.authenticatorAttachmentPreference, this.userVerificationRequirement });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
