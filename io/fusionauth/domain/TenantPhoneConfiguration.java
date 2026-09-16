package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import java.util.Objects;
import java.util.UUID;

public class TenantPhoneConfiguration implements Buildable<TenantPhoneConfiguration> {
  @ExcludeFromJSONColumn
  public UUID adminTwoFactorMethodRemoveTemplateId;
  
  @ExcludeFromJSONColumn
  public UUID forgotPasswordTemplateId;
  
  @ExcludeFromJSONColumn
  public UUID identityUpdateTemplateId;
  
  public boolean implicitPhoneVerificationAllowed = true;
  
  @ExcludeFromJSONColumn
  public UUID loginIdInUseOnCreateTemplateId;
  
  @ExcludeFromJSONColumn
  public UUID loginIdInUseOnUpdateTemplateId;
  
  @ExcludeFromJSONColumn
  public UUID loginNewDeviceTemplateId;
  
  @ExcludeFromJSONColumn
  public UUID loginSuspiciousTemplateId;
  
  @ExcludeFromJSONColumn
  public UUID messengerId;
  
  @ExcludeFromJSONColumn
  public UUID passwordResetSuccessTemplateId;
  
  @ExcludeFromJSONColumn
  public UUID passwordUpdateTemplateId;
  
  @ExcludeFromJSONColumn
  public UUID passwordlessTemplateId;
  
  @ExcludeFromJSONColumn
  public UUID setPasswordTemplateId;
  
  @ExcludeFromJSONColumn
  public UUID twoFactorMethodAddTemplateId;
  
  @ExcludeFromJSONColumn
  public UUID twoFactorMethodRemoveTemplateId;
  
  public PhoneUnverifiedOptions unverified = new PhoneUnverifiedOptions();
  
  @ExcludeFromJSONColumn
  public UUID verificationCompleteTemplateId;
  
  public VerificationStrategy verificationStrategy = VerificationStrategy.ClickableLink;
  
  @ExcludeFromJSONColumn
  public UUID verificationTemplateId;
  
  public boolean verifyPhoneNumber;
  
  @JacksonConstructor
  public TenantPhoneConfiguration() {}
  
  public TenantPhoneConfiguration(TenantPhoneConfiguration paramTenantPhoneConfiguration) {
    this.adminTwoFactorMethodRemoveTemplateId = paramTenantPhoneConfiguration.adminTwoFactorMethodRemoveTemplateId;
    this.forgotPasswordTemplateId = paramTenantPhoneConfiguration.forgotPasswordTemplateId;
    this.identityUpdateTemplateId = paramTenantPhoneConfiguration.identityUpdateTemplateId;
    this.implicitPhoneVerificationAllowed = paramTenantPhoneConfiguration.implicitPhoneVerificationAllowed;
    this.loginIdInUseOnCreateTemplateId = paramTenantPhoneConfiguration.loginIdInUseOnCreateTemplateId;
    this.loginIdInUseOnUpdateTemplateId = paramTenantPhoneConfiguration.loginIdInUseOnUpdateTemplateId;
    this.loginNewDeviceTemplateId = paramTenantPhoneConfiguration.loginNewDeviceTemplateId;
    this.loginSuspiciousTemplateId = paramTenantPhoneConfiguration.loginSuspiciousTemplateId;
    this.messengerId = paramTenantPhoneConfiguration.messengerId;
    this.passwordResetSuccessTemplateId = paramTenantPhoneConfiguration.passwordResetSuccessTemplateId;
    this.passwordUpdateTemplateId = paramTenantPhoneConfiguration.passwordUpdateTemplateId;
    this.passwordlessTemplateId = paramTenantPhoneConfiguration.passwordlessTemplateId;
    this.setPasswordTemplateId = paramTenantPhoneConfiguration.setPasswordTemplateId;
    this.twoFactorMethodAddTemplateId = paramTenantPhoneConfiguration.twoFactorMethodAddTemplateId;
    this.twoFactorMethodRemoveTemplateId = paramTenantPhoneConfiguration.twoFactorMethodRemoveTemplateId;
    this.unverified = new PhoneUnverifiedOptions(paramTenantPhoneConfiguration.unverified);
    this.verificationCompleteTemplateId = paramTenantPhoneConfiguration.verificationCompleteTemplateId;
    this.verificationStrategy = paramTenantPhoneConfiguration.verificationStrategy;
    this.verificationTemplateId = paramTenantPhoneConfiguration.verificationTemplateId;
    this.verifyPhoneNumber = paramTenantPhoneConfiguration.verifyPhoneNumber;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TenantPhoneConfiguration))
      return false; 
    TenantPhoneConfiguration tenantPhoneConfiguration = (TenantPhoneConfiguration)paramObject;
    return (this.implicitPhoneVerificationAllowed == tenantPhoneConfiguration.implicitPhoneVerificationAllowed && this.verifyPhoneNumber == tenantPhoneConfiguration.verifyPhoneNumber && 
      
      Objects.equals(this.adminTwoFactorMethodRemoveTemplateId, tenantPhoneConfiguration.adminTwoFactorMethodRemoveTemplateId) && 
      Objects.equals(this.forgotPasswordTemplateId, tenantPhoneConfiguration.forgotPasswordTemplateId) && 
      Objects.equals(this.identityUpdateTemplateId, tenantPhoneConfiguration.identityUpdateTemplateId) && 
      Objects.equals(this.loginIdInUseOnCreateTemplateId, tenantPhoneConfiguration.loginIdInUseOnCreateTemplateId) && 
      Objects.equals(this.loginIdInUseOnUpdateTemplateId, tenantPhoneConfiguration.loginIdInUseOnUpdateTemplateId) && 
      Objects.equals(this.loginNewDeviceTemplateId, tenantPhoneConfiguration.loginNewDeviceTemplateId) && 
      Objects.equals(this.loginSuspiciousTemplateId, tenantPhoneConfiguration.loginSuspiciousTemplateId) && 
      Objects.equals(this.messengerId, tenantPhoneConfiguration.messengerId) && 
      Objects.equals(this.passwordResetSuccessTemplateId, tenantPhoneConfiguration.passwordResetSuccessTemplateId) && 
      Objects.equals(this.passwordUpdateTemplateId, tenantPhoneConfiguration.passwordUpdateTemplateId) && 
      Objects.equals(this.passwordlessTemplateId, tenantPhoneConfiguration.passwordlessTemplateId) && 
      Objects.equals(this.setPasswordTemplateId, tenantPhoneConfiguration.setPasswordTemplateId) && 
      Objects.equals(this.twoFactorMethodAddTemplateId, tenantPhoneConfiguration.twoFactorMethodAddTemplateId) && 
      Objects.equals(this.twoFactorMethodRemoveTemplateId, tenantPhoneConfiguration.twoFactorMethodRemoveTemplateId) && 
      Objects.equals(this.unverified, tenantPhoneConfiguration.unverified) && 
      Objects.equals(this.verificationCompleteTemplateId, tenantPhoneConfiguration.verificationCompleteTemplateId) && this.verificationStrategy == tenantPhoneConfiguration.verificationStrategy && 
      
      Objects.equals(this.verificationTemplateId, tenantPhoneConfiguration.verificationTemplateId) && this.verifyPhoneNumber == tenantPhoneConfiguration.verifyPhoneNumber);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.adminTwoFactorMethodRemoveTemplateId, this.forgotPasswordTemplateId, this.identityUpdateTemplateId, Boolean.valueOf(this.implicitPhoneVerificationAllowed), this.loginIdInUseOnCreateTemplateId, this.loginIdInUseOnUpdateTemplateId, this.loginNewDeviceTemplateId, this.loginSuspiciousTemplateId, this.messengerId, this.passwordResetSuccessTemplateId, 
          this.passwordUpdateTemplateId, this.passwordlessTemplateId, this.setPasswordTemplateId, this.twoFactorMethodAddTemplateId, this.twoFactorMethodRemoveTemplateId, this.unverified, this.verificationCompleteTemplateId, this.verificationStrategy, this.verificationTemplateId, Boolean.valueOf(this.verifyPhoneNumber) });
  }
}
