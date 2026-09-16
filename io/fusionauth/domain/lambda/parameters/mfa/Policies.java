package io.fusionauth.domain.lambda.parameters.mfa;

import io.fusionauth.domain.ApplicationMultiFactorTrustPolicy;
import io.fusionauth.domain.MultiFactorLoginPolicy;

public class Policies {
  public final MultiFactorLoginPolicy applicationLoginPolicy;
  
  public final ApplicationMultiFactorTrustPolicy applicationMultiFactorTrustPolicy;
  
  public final MultiFactorLoginPolicy tenantLoginPolicy;
  
  public Policies(MultiFactorLoginPolicy paramMultiFactorLoginPolicy1, ApplicationMultiFactorTrustPolicy paramApplicationMultiFactorTrustPolicy, MultiFactorLoginPolicy paramMultiFactorLoginPolicy2) {
    this.applicationLoginPolicy = paramMultiFactorLoginPolicy1;
    this.tenantLoginPolicy = paramMultiFactorLoginPolicy2;
    this.applicationMultiFactorTrustPolicy = paramApplicationMultiFactorTrustPolicy;
  }
}
