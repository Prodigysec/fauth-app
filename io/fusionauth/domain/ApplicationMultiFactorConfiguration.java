package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;
import java.util.UUID;

public class ApplicationMultiFactorConfiguration {
  public MultiFactorEmailTemplate email = new MultiFactorEmailTemplate();
  
  public MultiFactorLoginPolicy loginPolicy;
  
  public MultiFactorSMSTemplate sms = new MultiFactorSMSTemplate();
  
  public ApplicationMultiFactorTrustPolicy trustPolicy;
  
  public MultiFactorVoiceTemplate voice = new MultiFactorVoiceTemplate();
  
  @JacksonConstructor
  public ApplicationMultiFactorConfiguration() {}
  
  public ApplicationMultiFactorConfiguration(ApplicationMultiFactorConfiguration paramApplicationMultiFactorConfiguration) {
    this.email = new MultiFactorEmailTemplate(paramApplicationMultiFactorConfiguration.email);
    this.loginPolicy = paramApplicationMultiFactorConfiguration.loginPolicy;
    this.sms = new MultiFactorSMSTemplate(paramApplicationMultiFactorConfiguration.sms);
    this.trustPolicy = paramApplicationMultiFactorConfiguration.trustPolicy;
    this.voice = new MultiFactorVoiceTemplate(paramApplicationMultiFactorConfiguration.voice);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    ApplicationMultiFactorConfiguration applicationMultiFactorConfiguration = (ApplicationMultiFactorConfiguration)paramObject;
    return (Objects.equals(this.email, applicationMultiFactorConfiguration.email) && this.loginPolicy == applicationMultiFactorConfiguration.loginPolicy && 
      
      Objects.equals(this.sms, applicationMultiFactorConfiguration.sms) && this.trustPolicy == applicationMultiFactorConfiguration.trustPolicy && 
      
      Objects.equals(this.voice, applicationMultiFactorConfiguration.voice));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.email, this.loginPolicy, this.sms, this.trustPolicy, this.voice });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public static class MultiFactorEmailTemplate {
    public UUID templateId;
    
    @JacksonConstructor
    public MultiFactorEmailTemplate() {}
    
    public MultiFactorEmailTemplate(MultiFactorEmailTemplate param1MultiFactorEmailTemplate) {
      this.templateId = param1MultiFactorEmailTemplate.templateId;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      MultiFactorEmailTemplate multiFactorEmailTemplate = (MultiFactorEmailTemplate)param1Object;
      return Objects.equals(this.templateId, multiFactorEmailTemplate.templateId);
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.templateId });
    }
  }
  
  public static class MultiFactorSMSTemplate {
    public UUID templateId;
    
    @JacksonConstructor
    public MultiFactorSMSTemplate() {}
    
    public MultiFactorSMSTemplate(MultiFactorSMSTemplate param1MultiFactorSMSTemplate) {
      this.templateId = param1MultiFactorSMSTemplate.templateId;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      MultiFactorSMSTemplate multiFactorSMSTemplate = (MultiFactorSMSTemplate)param1Object;
      return Objects.equals(this.templateId, multiFactorSMSTemplate.templateId);
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.templateId });
    }
  }
  
  public static class MultiFactorVoiceTemplate {
    public UUID templateId;
    
    @JacksonConstructor
    public MultiFactorVoiceTemplate() {}
    
    public MultiFactorVoiceTemplate(MultiFactorVoiceTemplate param1MultiFactorVoiceTemplate) {
      this.templateId = param1MultiFactorVoiceTemplate.templateId;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      MultiFactorVoiceTemplate multiFactorVoiceTemplate = (MultiFactorVoiceTemplate)param1Object;
      return Objects.equals(this.templateId, multiFactorVoiceTemplate.templateId);
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.templateId });
    }
  }
}
