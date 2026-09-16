package io.fusionauth.api.service.risk;

import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.domain.ClientRiskConfiguration;
import io.fusionauth.domain.User;
import javax.annotation.Nonnull;

public final class RiskSignalContext {
  public final Double botDetectionScore;
  
  public final ClientRiskConfiguration clientRiskConfiguration;
  
  public final String ipAddress;
  
  public final ExternalIdentifier mfaTrust;
  
  public final boolean newDevice;
  
  @Nonnull
  public final User user;
  
  public final String userAgent;
  
  public RiskSignalContext(@Nonnull User paramUser, String paramString1, String paramString2, ExternalIdentifier paramExternalIdentifier, Double paramDouble, boolean paramBoolean, ClientRiskConfiguration paramClientRiskConfiguration) {
    this.user = paramUser;
    this.ipAddress = paramString1;
    this.userAgent = paramString2;
    this.mfaTrust = paramExternalIdentifier;
    this.botDetectionScore = paramDouble;
    this.newDevice = paramBoolean;
    this.clientRiskConfiguration = paramClientRiskConfiguration;
  }
}
