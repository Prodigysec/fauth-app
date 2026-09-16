package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;
import java.util.UUID;

public class TenantMultiFactorConfiguration implements Buildable<TenantMultiFactorConfiguration> {
  public MultiFactorAuthenticatorMethod authenticator;
  
  public boolean debug;
  
  public MultiFactorEmailMethod email;
  
  public MultiFactorLoginPolicy loginPolicy;
  
  public MultiFactorSMSMethod sms;
  
  public MultiFactorVoiceMethod voice;
  
  @JacksonConstructor
  public TenantMultiFactorConfiguration() {
    this.authenticator = (new MultiFactorAuthenticatorMethod()).with(paramMultiFactorAuthenticatorMethod -> paramMultiFactorAuthenticatorMethod.enabled = true);
    this.email = new MultiFactorEmailMethod();
    this.loginPolicy = MultiFactorLoginPolicy.Enabled;
    this.sms = new MultiFactorSMSMethod();
    this.voice = new MultiFactorVoiceMethod();
  }
  
  public TenantMultiFactorConfiguration(TenantMultiFactorConfiguration paramTenantMultiFactorConfiguration) {
    this.authenticator = (new MultiFactorAuthenticatorMethod()).with(paramMultiFactorAuthenticatorMethod -> paramMultiFactorAuthenticatorMethod.enabled = true);
    this.email = new MultiFactorEmailMethod();
    this.loginPolicy = MultiFactorLoginPolicy.Enabled;
    this.sms = new MultiFactorSMSMethod();
    this.voice = new MultiFactorVoiceMethod();
    this.authenticator = new MultiFactorAuthenticatorMethod(paramTenantMultiFactorConfiguration.authenticator);
    this.debug = paramTenantMultiFactorConfiguration.debug;
    this.email = new MultiFactorEmailMethod(paramTenantMultiFactorConfiguration.email);
    this.loginPolicy = paramTenantMultiFactorConfiguration.loginPolicy;
    this.sms = new MultiFactorSMSMethod(paramTenantMultiFactorConfiguration.sms);
    this.voice = new MultiFactorVoiceMethod(paramTenantMultiFactorConfiguration.voice);
  }
  
  @JsonIgnore
  public boolean anyEnabled() {
    return (this.authenticator.enabled || this.sms.enabled || this.email.enabled || this.voice.enabled);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    TenantMultiFactorConfiguration tenantMultiFactorConfiguration = (TenantMultiFactorConfiguration)paramObject;
    return (Objects.equals(this.authenticator, tenantMultiFactorConfiguration.authenticator) && this.debug == tenantMultiFactorConfiguration.debug && 
      
      Objects.equals(this.email, tenantMultiFactorConfiguration.email) && this.loginPolicy == tenantMultiFactorConfiguration.loginPolicy && 
      
      Objects.equals(this.sms, tenantMultiFactorConfiguration.sms) && 
      Objects.equals(this.voice, tenantMultiFactorConfiguration.voice));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.authenticator, Boolean.valueOf(this.debug), this.email, this.loginPolicy, this.sms, this.voice });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public static class MultiFactorAuthenticatorMethod extends Enableable implements Buildable<MultiFactorAuthenticatorMethod> {
    public AuthenticatorConfiguration.TOTPAlgorithm algorithm = AuthenticatorConfiguration.TOTPAlgorithm.HmacSHA1;
    
    public int codeLength = 6;
    
    public int timeStep = 30;
    
    @JacksonConstructor
    public MultiFactorAuthenticatorMethod() {}
    
    public MultiFactorAuthenticatorMethod(MultiFactorAuthenticatorMethod param1MultiFactorAuthenticatorMethod) {
      this.algorithm = param1MultiFactorAuthenticatorMethod.algorithm;
      this.codeLength = param1MultiFactorAuthenticatorMethod.codeLength;
      this.enabled = param1MultiFactorAuthenticatorMethod.enabled;
      this.timeStep = param1MultiFactorAuthenticatorMethod.timeStep;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      if (!super.equals(param1Object))
        return false; 
      MultiFactorAuthenticatorMethod multiFactorAuthenticatorMethod = (MultiFactorAuthenticatorMethod)param1Object;
      return (this.codeLength == multiFactorAuthenticatorMethod.codeLength && this.timeStep == multiFactorAuthenticatorMethod.timeStep && this.algorithm == multiFactorAuthenticatorMethod.algorithm);
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.algorithm, Integer.valueOf(this.codeLength), Integer.valueOf(this.timeStep) });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
  
  public static class MultiFactorEmailMethod extends Enableable implements Buildable<MultiFactorEmailMethod> {
    public UUID templateId;
    
    @JacksonConstructor
    public MultiFactorEmailMethod() {}
    
    public MultiFactorEmailMethod(MultiFactorEmailMethod param1MultiFactorEmailMethod) {
      this.enabled = param1MultiFactorEmailMethod.enabled;
      this.templateId = param1MultiFactorEmailMethod.templateId;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      if (!super.equals(param1Object))
        return false; 
      MultiFactorEmailMethod multiFactorEmailMethod = (MultiFactorEmailMethod)param1Object;
      return Objects.equals(this.templateId, multiFactorEmailMethod.templateId);
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.templateId });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
  
  public static class MultiFactorSMSMethod extends Enableable implements Buildable<MultiFactorSMSMethod> {
    public UUID messengerId;
    
    public UUID templateId;
    
    @JacksonConstructor
    public MultiFactorSMSMethod() {}
    
    public MultiFactorSMSMethod(MultiFactorSMSMethod param1MultiFactorSMSMethod) {
      this.enabled = param1MultiFactorSMSMethod.enabled;
      this.messengerId = param1MultiFactorSMSMethod.messengerId;
      this.templateId = param1MultiFactorSMSMethod.templateId;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      if (!super.equals(param1Object))
        return false; 
      MultiFactorSMSMethod multiFactorSMSMethod = (MultiFactorSMSMethod)param1Object;
      return (Objects.equals(this.messengerId, multiFactorSMSMethod.messengerId) && Objects.equals(this.templateId, multiFactorSMSMethod.templateId));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.messengerId, this.templateId });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
  
  public static class MultiFactorVoiceMethod extends Enableable implements Buildable<MultiFactorVoiceMethod> {
    public UUID messengerId;
    
    public UUID templateId;
    
    @JacksonConstructor
    public MultiFactorVoiceMethod() {}
    
    public MultiFactorVoiceMethod(MultiFactorVoiceMethod param1MultiFactorVoiceMethod) {
      this.enabled = param1MultiFactorVoiceMethod.enabled;
      this.messengerId = param1MultiFactorVoiceMethod.messengerId;
      this.templateId = param1MultiFactorVoiceMethod.templateId;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      if (!super.equals(param1Object))
        return false; 
      MultiFactorVoiceMethod multiFactorVoiceMethod = (MultiFactorVoiceMethod)param1Object;
      return (Objects.equals(this.messengerId, multiFactorVoiceMethod.messengerId) && Objects.equals(this.templateId, multiFactorVoiceMethod.templateId));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.messengerId, this.templateId });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
}
