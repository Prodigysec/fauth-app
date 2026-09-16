package io.fusionauth.domain.lambda.parameters.mfa;

public class RequiredLambdaResult {
  public boolean required;
  
  public boolean sendSuspiciousLoginEvent;
  
  public RequiredLambdaResult(boolean paramBoolean) {
    this.required = paramBoolean;
  }
}
