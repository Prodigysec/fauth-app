package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.webauthn.AuthenticatorAttachmentPreference;
import io.fusionauth.domain.webauthn.UserVerificationRequirement;
import java.util.Objects;

public class TenantWebAuthnConfiguration extends Enableable implements Buildable<TenantWebAuthnConfiguration> {
  public TenantWebAuthnWorkflowConfiguration bootstrapWorkflow;
  
  public boolean debug;
  
  public TenantWebAuthnWorkflowConfiguration reauthenticationWorkflow;
  
  public String relyingPartyId;
  
  public String relyingPartyName;
  
  @JacksonConstructor
  public TenantWebAuthnConfiguration() {
    this
      .bootstrapWorkflow = (new TenantWebAuthnWorkflowConfiguration()).with(paramTenantWebAuthnWorkflowConfiguration -> paramTenantWebAuthnWorkflowConfiguration.authenticatorAttachmentPreference = AuthenticatorAttachmentPreference.any).with(paramTenantWebAuthnWorkflowConfiguration -> paramTenantWebAuthnWorkflowConfiguration.userVerificationRequirement = UserVerificationRequirement.required);
    this
      .reauthenticationWorkflow = (new TenantWebAuthnWorkflowConfiguration()).with(paramTenantWebAuthnWorkflowConfiguration -> paramTenantWebAuthnWorkflowConfiguration.authenticatorAttachmentPreference = AuthenticatorAttachmentPreference.platform).with(paramTenantWebAuthnWorkflowConfiguration -> paramTenantWebAuthnWorkflowConfiguration.userVerificationRequirement = UserVerificationRequirement.required);
  }
  
  public TenantWebAuthnConfiguration(TenantWebAuthnConfiguration paramTenantWebAuthnConfiguration) {
    this.bootstrapWorkflow = (new TenantWebAuthnWorkflowConfiguration()).with(paramTenantWebAuthnWorkflowConfiguration -> paramTenantWebAuthnWorkflowConfiguration.authenticatorAttachmentPreference = AuthenticatorAttachmentPreference.any).with(paramTenantWebAuthnWorkflowConfiguration -> paramTenantWebAuthnWorkflowConfiguration.userVerificationRequirement = UserVerificationRequirement.required);
    this.reauthenticationWorkflow = (new TenantWebAuthnWorkflowConfiguration()).with(paramTenantWebAuthnWorkflowConfiguration -> paramTenantWebAuthnWorkflowConfiguration.authenticatorAttachmentPreference = AuthenticatorAttachmentPreference.platform).with(paramTenantWebAuthnWorkflowConfiguration -> paramTenantWebAuthnWorkflowConfiguration.userVerificationRequirement = UserVerificationRequirement.required);
    this.bootstrapWorkflow = new TenantWebAuthnWorkflowConfiguration(paramTenantWebAuthnConfiguration.bootstrapWorkflow);
    this.debug = paramTenantWebAuthnConfiguration.debug;
    this.enabled = paramTenantWebAuthnConfiguration.enabled;
    this.reauthenticationWorkflow = new TenantWebAuthnWorkflowConfiguration(paramTenantWebAuthnConfiguration.reauthenticationWorkflow);
    this.relyingPartyId = paramTenantWebAuthnConfiguration.relyingPartyId;
    this.relyingPartyName = paramTenantWebAuthnConfiguration.relyingPartyName;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TenantWebAuthnConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    TenantWebAuthnConfiguration tenantWebAuthnConfiguration = (TenantWebAuthnConfiguration)paramObject;
    return (this.debug == tenantWebAuthnConfiguration.debug && 
      Objects.equals(this.bootstrapWorkflow, tenantWebAuthnConfiguration.bootstrapWorkflow) && 
      Objects.equals(this.reauthenticationWorkflow, tenantWebAuthnConfiguration.reauthenticationWorkflow) && 
      Objects.equals(this.relyingPartyId, tenantWebAuthnConfiguration.relyingPartyId) && 
      Objects.equals(this.relyingPartyName, tenantWebAuthnConfiguration.relyingPartyName));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), Boolean.valueOf(this.debug), this.bootstrapWorkflow, this.reauthenticationWorkflow, this.relyingPartyId, this.relyingPartyName });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
