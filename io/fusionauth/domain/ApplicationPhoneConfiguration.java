package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import java.util.Objects;
import java.util.UUID;

public class ApplicationPhoneConfiguration implements Buildable<ApplicationPhoneConfiguration> {
  public UUID forgotPasswordTemplateId;
  
  public UUID identityUpdateTemplateId;
  
  public UUID loginIdInUseOnCreateTemplateId;
  
  public UUID loginIdInUseOnUpdateTemplateId;
  
  public UUID loginNewDeviceTemplateId;
  
  public UUID loginSuspiciousTemplateId;
  
  public UUID passwordResetSuccessTemplateId;
  
  public UUID passwordUpdateTemplateId;
  
  public UUID passwordlessTemplateId;
  
  public UUID setPasswordTemplateId;
  
  public UUID twoFactorMethodAddTemplateId;
  
  public UUID twoFactorMethodRemoveTemplateId;
  
  public UUID verificationCompleteTemplateId;
  
  public UUID verificationTemplateId;
  
  @JacksonConstructor
  public ApplicationPhoneConfiguration() {}
  
  public ApplicationPhoneConfiguration(ApplicationPhoneConfiguration paramApplicationPhoneConfiguration) {
    this.forgotPasswordTemplateId = paramApplicationPhoneConfiguration.forgotPasswordTemplateId;
    this.identityUpdateTemplateId = paramApplicationPhoneConfiguration.identityUpdateTemplateId;
    this.loginIdInUseOnCreateTemplateId = paramApplicationPhoneConfiguration.loginIdInUseOnCreateTemplateId;
    this.loginIdInUseOnUpdateTemplateId = paramApplicationPhoneConfiguration.loginIdInUseOnUpdateTemplateId;
    this.loginNewDeviceTemplateId = paramApplicationPhoneConfiguration.loginNewDeviceTemplateId;
    this.loginSuspiciousTemplateId = paramApplicationPhoneConfiguration.loginSuspiciousTemplateId;
    this.passwordResetSuccessTemplateId = paramApplicationPhoneConfiguration.passwordResetSuccessTemplateId;
    this.passwordUpdateTemplateId = paramApplicationPhoneConfiguration.passwordUpdateTemplateId;
    this.passwordlessTemplateId = paramApplicationPhoneConfiguration.passwordlessTemplateId;
    this.setPasswordTemplateId = paramApplicationPhoneConfiguration.setPasswordTemplateId;
    this.twoFactorMethodAddTemplateId = paramApplicationPhoneConfiguration.twoFactorMethodAddTemplateId;
    this.twoFactorMethodRemoveTemplateId = paramApplicationPhoneConfiguration.twoFactorMethodRemoveTemplateId;
    this.verificationCompleteTemplateId = paramApplicationPhoneConfiguration.verificationCompleteTemplateId;
    this.verificationTemplateId = paramApplicationPhoneConfiguration.verificationTemplateId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof ApplicationPhoneConfiguration))
      return false; 
    ApplicationPhoneConfiguration applicationPhoneConfiguration = (ApplicationPhoneConfiguration)paramObject;
    return (Objects.equals(this.forgotPasswordTemplateId, applicationPhoneConfiguration.forgotPasswordTemplateId) && 
      Objects.equals(this.identityUpdateTemplateId, applicationPhoneConfiguration.identityUpdateTemplateId) && 
      Objects.equals(this.loginIdInUseOnCreateTemplateId, applicationPhoneConfiguration.loginIdInUseOnCreateTemplateId) && 
      Objects.equals(this.loginIdInUseOnUpdateTemplateId, applicationPhoneConfiguration.loginIdInUseOnUpdateTemplateId) && 
      Objects.equals(this.loginNewDeviceTemplateId, applicationPhoneConfiguration.loginNewDeviceTemplateId) && 
      Objects.equals(this.loginSuspiciousTemplateId, applicationPhoneConfiguration.loginSuspiciousTemplateId) && 
      Objects.equals(this.passwordResetSuccessTemplateId, applicationPhoneConfiguration.passwordResetSuccessTemplateId) && 
      Objects.equals(this.passwordUpdateTemplateId, applicationPhoneConfiguration.passwordUpdateTemplateId) && 
      Objects.equals(this.passwordlessTemplateId, applicationPhoneConfiguration.passwordlessTemplateId) && 
      Objects.equals(this.setPasswordTemplateId, applicationPhoneConfiguration.setPasswordTemplateId) && 
      Objects.equals(this.twoFactorMethodAddTemplateId, applicationPhoneConfiguration.twoFactorMethodAddTemplateId) && 
      Objects.equals(this.twoFactorMethodRemoveTemplateId, applicationPhoneConfiguration.twoFactorMethodRemoveTemplateId) && 
      Objects.equals(this.verificationCompleteTemplateId, applicationPhoneConfiguration.verificationCompleteTemplateId) && 
      Objects.equals(this.verificationTemplateId, applicationPhoneConfiguration.verificationTemplateId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.forgotPasswordTemplateId, this.identityUpdateTemplateId, this.loginIdInUseOnCreateTemplateId, this.loginIdInUseOnUpdateTemplateId, this.loginNewDeviceTemplateId, this.loginSuspiciousTemplateId, this.passwordResetSuccessTemplateId, this.passwordUpdateTemplateId, this.passwordlessTemplateId, this.setPasswordTemplateId, 
          this.twoFactorMethodAddTemplateId, this.twoFactorMethodRemoveTemplateId, this.verificationCompleteTemplateId, this.verificationTemplateId });
  }
}
