package io.fusionauth.domain;

import com.inversoft.json.ToString;
import java.util.Objects;

public class ClientRiskConfiguration extends Enableable implements Buildable<ClientRiskConfiguration> {
  public boolean blocklistedIp = true;
  
  public boolean botDetected = true;
  
  public boolean dormantAccount = true;
  
  public boolean dormantPassword = true;
  
  public boolean impossibleTravel = true;
  
  public boolean recentIdentityChange = true;
  
  public boolean recentPasswordChange = true;
  
  public boolean suspiciousUserAgent = true;
  
  public boolean unrecognizedDevice = true;
  
  public boolean untrustedDevice = true;
  
  public ClientRiskConfiguration() {}
  
  public ClientRiskConfiguration(ClientRiskConfiguration paramClientRiskConfiguration) {
    this.enabled = paramClientRiskConfiguration.enabled;
    this.botDetected = paramClientRiskConfiguration.botDetected;
    this.dormantAccount = paramClientRiskConfiguration.dormantAccount;
    this.impossibleTravel = paramClientRiskConfiguration.impossibleTravel;
    this.blocklistedIp = paramClientRiskConfiguration.blocklistedIp;
    this.unrecognizedDevice = paramClientRiskConfiguration.unrecognizedDevice;
    this.recentIdentityChange = paramClientRiskConfiguration.recentIdentityChange;
    this.dormantPassword = paramClientRiskConfiguration.dormantPassword;
    this.recentPasswordChange = paramClientRiskConfiguration.recentPasswordChange;
    this.untrustedDevice = paramClientRiskConfiguration.untrustedDevice;
    this.suspiciousUserAgent = paramClientRiskConfiguration.suspiciousUserAgent;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof ClientRiskConfiguration))
      return false; 
    ClientRiskConfiguration clientRiskConfiguration = (ClientRiskConfiguration)paramObject;
    return (this.enabled == clientRiskConfiguration.enabled && this.blocklistedIp == clientRiskConfiguration.blocklistedIp && this.botDetected == clientRiskConfiguration.botDetected && this.dormantAccount == clientRiskConfiguration.dormantAccount && this.dormantPassword == clientRiskConfiguration.dormantPassword && this.impossibleTravel == clientRiskConfiguration.impossibleTravel && this.recentIdentityChange == clientRiskConfiguration.recentIdentityChange && this.recentPasswordChange == clientRiskConfiguration.recentPasswordChange && this.suspiciousUserAgent == clientRiskConfiguration.suspiciousUserAgent && this.unrecognizedDevice == clientRiskConfiguration.unrecognizedDevice && this.untrustedDevice == clientRiskConfiguration.untrustedDevice);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          Boolean.valueOf(this.enabled), Boolean.valueOf(this.blocklistedIp), Boolean.valueOf(this.botDetected), Boolean.valueOf(this.dormantAccount), Boolean.valueOf(this.dormantPassword), Boolean.valueOf(this.impossibleTravel), Boolean.valueOf(this.recentIdentityChange), 
          Boolean.valueOf(this.recentPasswordChange), Boolean.valueOf(this.suspiciousUserAgent), Boolean.valueOf(this.unrecognizedDevice), 
          Boolean.valueOf(this.untrustedDevice) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
