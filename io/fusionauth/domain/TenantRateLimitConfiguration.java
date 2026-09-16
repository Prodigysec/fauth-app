package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class TenantRateLimitConfiguration implements Buildable<TenantRateLimitConfiguration> {
  public RateLimitedRequestConfiguration failedLogin = new RateLimitedRequestConfiguration(5, 60);
  
  public RateLimitedRequestConfiguration forgotPassword = new RateLimitedRequestConfiguration(5, 60);
  
  public RateLimitedRequestConfiguration sendEmailVerification = new RateLimitedRequestConfiguration(5, 60);
  
  public RateLimitedRequestConfiguration sendPasswordless = new RateLimitedRequestConfiguration(5, 60);
  
  public RateLimitedRequestConfiguration sendPasswordlessPhone = new RateLimitedRequestConfiguration(5, 60);
  
  public RateLimitedRequestConfiguration sendPhoneVerification = new RateLimitedRequestConfiguration(5, 60);
  
  public RateLimitedRequestConfiguration sendRegistrationVerification = new RateLimitedRequestConfiguration(5, 60);
  
  public RateLimitedRequestConfiguration sendTwoFactor = new RateLimitedRequestConfiguration(5, 60);
  
  @JacksonConstructor
  public TenantRateLimitConfiguration() {}
  
  public TenantRateLimitConfiguration(TenantRateLimitConfiguration paramTenantRateLimitConfiguration) {
    this.failedLogin = new RateLimitedRequestConfiguration(paramTenantRateLimitConfiguration.failedLogin);
    this.forgotPassword = new RateLimitedRequestConfiguration(paramTenantRateLimitConfiguration.forgotPassword);
    this.sendEmailVerification = new RateLimitedRequestConfiguration(paramTenantRateLimitConfiguration.sendEmailVerification);
    this.sendPasswordless = new RateLimitedRequestConfiguration(paramTenantRateLimitConfiguration.sendPasswordless);
    this.sendRegistrationVerification = new RateLimitedRequestConfiguration(paramTenantRateLimitConfiguration.sendRegistrationVerification);
    this.sendPhoneVerification = new RateLimitedRequestConfiguration(paramTenantRateLimitConfiguration.sendPhoneVerification);
    this.sendTwoFactor = new RateLimitedRequestConfiguration(paramTenantRateLimitConfiguration.sendTwoFactor);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    TenantRateLimitConfiguration tenantRateLimitConfiguration = (TenantRateLimitConfiguration)paramObject;
    return (Objects.equals(this.failedLogin, tenantRateLimitConfiguration.failedLogin) && 
      Objects.equals(this.forgotPassword, tenantRateLimitConfiguration.forgotPassword) && 
      Objects.equals(this.sendEmailVerification, tenantRateLimitConfiguration.sendEmailVerification) && 
      Objects.equals(this.sendPasswordless, tenantRateLimitConfiguration.sendPasswordless) && 
      Objects.equals(this.sendPhoneVerification, tenantRateLimitConfiguration.sendPhoneVerification) && 
      Objects.equals(this.sendRegistrationVerification, tenantRateLimitConfiguration.sendRegistrationVerification) && 
      Objects.equals(this.sendTwoFactor, tenantRateLimitConfiguration.sendTwoFactor));
  }
  
  @Deprecated
  @JsonIgnore
  public RateLimitedRequestConfiguration getConfiguration(RateLimitedRequestType paramRateLimitedRequestType) {
    switch (paramRateLimitedRequestType) {
      case FailedLogin:
        return this.failedLogin;
      case ForgotPassword:
        return this.forgotPassword;
      case SendEmailVerification:
        return this.sendEmailVerification;
      case SendPasswordless:
        return this.sendPasswordless;
      case SendPhonePasswordless:
        return this.sendPasswordlessPhone;
      case SendPhoneVerification:
        return this.sendPhoneVerification;
      case SendRegistrationVerification:
        return this.sendRegistrationVerification;
      case SendTwoFactor:
        return this.sendTwoFactor;
    } 
    throw new IllegalArgumentException("Unexpected request type [" + String.valueOf(paramRateLimitedRequestType) + "].");
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.failedLogin, this.forgotPassword, this.sendEmailVerification, this.sendPasswordless, this.sendPhoneVerification, this.sendRegistrationVerification, this.sendTwoFactor });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
