package io.fusionauth.domain.lambda.parameters.mfa;

public class ClientRisk {
  public final String status;
  
  public ClientRisk(String paramString) {
    this.status = paramString;
  }
}
