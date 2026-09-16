package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class ExternalIdentifierConfiguration implements Buildable<ExternalIdentifierConfiguration> {
  public int authorizationGrantIdTimeToLiveInSeconds = 30;
  
  public SecureGeneratorConfiguration changePasswordIdGenerator = new SecureGeneratorConfiguration(32, SecureGeneratorType.randomBytes);
  
  public int changePasswordIdTimeToLiveInSeconds = 600;
  
  public int deviceCodeTimeToLiveInSeconds = 300;
  
  public SecureGeneratorConfiguration deviceUserCodeIdGenerator = new SecureGeneratorConfiguration(6, SecureGeneratorType.randomAlphaNumeric);
  
  public SecureGeneratorConfiguration emailVerificationIdGenerator = new SecureGeneratorConfiguration(32, SecureGeneratorType.randomBytes);
  
  public int emailVerificationIdTimeToLiveInSeconds = 86400;
  
  public SecureGeneratorConfiguration emailVerificationOneTimeCodeGenerator = new SecureGeneratorConfiguration(6, SecureGeneratorType.randomAlphaNumeric);
  
  public int externalAuthenticationIdTimeToLiveInSeconds = 300;
  
  public int identityProviderConnectionTestTimeToLiveInSeconds = 1800;
  
  public int loginIntentTimeToLiveInSeconds = 1800;
  
  public int oneTimePasswordTimeToLiveInSeconds = 60;
  
  public SecureGeneratorConfiguration passwordlessLoginGenerator = new SecureGeneratorConfiguration(32, SecureGeneratorType.randomBytes);
  
  public SecureGeneratorConfiguration passwordlessLoginOneTimeCodeGenerator = new SecureGeneratorConfiguration(6, SecureGeneratorType.randomDigits);
  
  public int passwordlessLoginTimeToLiveInSeconds = 180;
  
  public int pendingAccountLinkTimeToLiveInSeconds = 3600;
  
  public SecureGeneratorConfiguration phoneVerificationIdGenerator = new SecureGeneratorConfiguration(32, SecureGeneratorType.randomBytes);
  
  public int phoneVerificationIdTimeToLiveInSeconds = 86400;
  
  public SecureGeneratorConfiguration phoneVerificationOneTimeCodeGenerator = new SecureGeneratorConfiguration(6, SecureGeneratorType.randomAlphaNumeric);
  
  public SecureGeneratorConfiguration registrationVerificationIdGenerator = new SecureGeneratorConfiguration(32, SecureGeneratorType.randomBytes);
  
  public int registrationVerificationIdTimeToLiveInSeconds = 86400;
  
  public SecureGeneratorConfiguration registrationVerificationOneTimeCodeGenerator = new SecureGeneratorConfiguration(6, SecureGeneratorType.randomAlphaNumeric);
  
  public int rememberOAuthScopeConsentChoiceTimeToLiveInSeconds = 2592000;
  
  public int samlv2AuthNRequestIdTimeToLiveInSeconds = 300;
  
  public SecureGeneratorConfiguration setupPasswordIdGenerator = new SecureGeneratorConfiguration(32, SecureGeneratorType.randomBytes);
  
  public int setupPasswordIdTimeToLiveInSeconds = 86400;
  
  public int trustTokenTimeToLiveInSeconds = 180;
  
  public int twoFactorIdTimeToLiveInSeconds = 300;
  
  public SecureGeneratorConfiguration twoFactorOneTimeCodeIdGenerator = new SecureGeneratorConfiguration(6, SecureGeneratorType.randomDigits);
  
  public int twoFactorOneTimeCodeIdTimeToLiveInSeconds = 60;
  
  public int twoFactorTrustIdTimeToLiveInSeconds = 2592000;
  
  public int webAuthnAuthenticationChallengeTimeToLiveInSeconds = 180;
  
  public int webAuthnRegistrationChallengeTimeToLiveInSeconds = 180;
  
  @JacksonConstructor
  public ExternalIdentifierConfiguration() {}
  
  public ExternalIdentifierConfiguration(ExternalIdentifierConfiguration paramExternalIdentifierConfiguration) {
    this.authorizationGrantIdTimeToLiveInSeconds = paramExternalIdentifierConfiguration.authorizationGrantIdTimeToLiveInSeconds;
    this.changePasswordIdGenerator = new SecureGeneratorConfiguration(paramExternalIdentifierConfiguration.changePasswordIdGenerator);
    this.changePasswordIdTimeToLiveInSeconds = paramExternalIdentifierConfiguration.changePasswordIdTimeToLiveInSeconds;
    this.deviceCodeTimeToLiveInSeconds = paramExternalIdentifierConfiguration.deviceCodeTimeToLiveInSeconds;
    this.deviceUserCodeIdGenerator = new SecureGeneratorConfiguration(paramExternalIdentifierConfiguration.deviceUserCodeIdGenerator);
    this.emailVerificationIdGenerator = new SecureGeneratorConfiguration(paramExternalIdentifierConfiguration.emailVerificationIdGenerator);
    this.emailVerificationIdTimeToLiveInSeconds = paramExternalIdentifierConfiguration.emailVerificationIdTimeToLiveInSeconds;
    this.emailVerificationOneTimeCodeGenerator = new SecureGeneratorConfiguration(paramExternalIdentifierConfiguration.emailVerificationOneTimeCodeGenerator);
    this.externalAuthenticationIdTimeToLiveInSeconds = paramExternalIdentifierConfiguration.externalAuthenticationIdTimeToLiveInSeconds;
    this.identityProviderConnectionTestTimeToLiveInSeconds = paramExternalIdentifierConfiguration.identityProviderConnectionTestTimeToLiveInSeconds;
    this.phoneVerificationIdGenerator = new SecureGeneratorConfiguration(paramExternalIdentifierConfiguration.phoneVerificationIdGenerator);
    this.phoneVerificationIdTimeToLiveInSeconds = paramExternalIdentifierConfiguration.phoneVerificationIdTimeToLiveInSeconds;
    this.phoneVerificationOneTimeCodeGenerator = new SecureGeneratorConfiguration(paramExternalIdentifierConfiguration.phoneVerificationOneTimeCodeGenerator);
    this.loginIntentTimeToLiveInSeconds = paramExternalIdentifierConfiguration.loginIntentTimeToLiveInSeconds;
    this.oneTimePasswordTimeToLiveInSeconds = paramExternalIdentifierConfiguration.oneTimePasswordTimeToLiveInSeconds;
    this.passwordlessLoginTimeToLiveInSeconds = paramExternalIdentifierConfiguration.passwordlessLoginTimeToLiveInSeconds;
    this.passwordlessLoginGenerator = new SecureGeneratorConfiguration(paramExternalIdentifierConfiguration.passwordlessLoginGenerator);
    this.passwordlessLoginOneTimeCodeGenerator = new SecureGeneratorConfiguration(paramExternalIdentifierConfiguration.passwordlessLoginOneTimeCodeGenerator);
    this.pendingAccountLinkTimeToLiveInSeconds = paramExternalIdentifierConfiguration.pendingAccountLinkTimeToLiveInSeconds;
    this.registrationVerificationIdGenerator = new SecureGeneratorConfiguration(paramExternalIdentifierConfiguration.registrationVerificationIdGenerator);
    this.registrationVerificationIdTimeToLiveInSeconds = paramExternalIdentifierConfiguration.registrationVerificationIdTimeToLiveInSeconds;
    this.registrationVerificationOneTimeCodeGenerator = new SecureGeneratorConfiguration(paramExternalIdentifierConfiguration.registrationVerificationOneTimeCodeGenerator);
    this.rememberOAuthScopeConsentChoiceTimeToLiveInSeconds = paramExternalIdentifierConfiguration.rememberOAuthScopeConsentChoiceTimeToLiveInSeconds;
    this.samlv2AuthNRequestIdTimeToLiveInSeconds = paramExternalIdentifierConfiguration.samlv2AuthNRequestIdTimeToLiveInSeconds;
    this.setupPasswordIdGenerator = new SecureGeneratorConfiguration(paramExternalIdentifierConfiguration.setupPasswordIdGenerator);
    this.setupPasswordIdTimeToLiveInSeconds = paramExternalIdentifierConfiguration.setupPasswordIdTimeToLiveInSeconds;
    this.trustTokenTimeToLiveInSeconds = paramExternalIdentifierConfiguration.trustTokenTimeToLiveInSeconds;
    this.twoFactorIdTimeToLiveInSeconds = paramExternalIdentifierConfiguration.twoFactorIdTimeToLiveInSeconds;
    this.twoFactorOneTimeCodeIdGenerator = new SecureGeneratorConfiguration(paramExternalIdentifierConfiguration.twoFactorOneTimeCodeIdGenerator);
    this.twoFactorOneTimeCodeIdTimeToLiveInSeconds = paramExternalIdentifierConfiguration.twoFactorOneTimeCodeIdTimeToLiveInSeconds;
    this.twoFactorTrustIdTimeToLiveInSeconds = paramExternalIdentifierConfiguration.twoFactorTrustIdTimeToLiveInSeconds;
    this.webAuthnAuthenticationChallengeTimeToLiveInSeconds = paramExternalIdentifierConfiguration.webAuthnAuthenticationChallengeTimeToLiveInSeconds;
    this.webAuthnRegistrationChallengeTimeToLiveInSeconds = paramExternalIdentifierConfiguration.webAuthnRegistrationChallengeTimeToLiveInSeconds;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    ExternalIdentifierConfiguration externalIdentifierConfiguration = (ExternalIdentifierConfiguration)paramObject;
    return (this.authorizationGrantIdTimeToLiveInSeconds == externalIdentifierConfiguration.authorizationGrantIdTimeToLiveInSeconds && this.changePasswordIdTimeToLiveInSeconds == externalIdentifierConfiguration.changePasswordIdTimeToLiveInSeconds && this.deviceCodeTimeToLiveInSeconds == externalIdentifierConfiguration.deviceCodeTimeToLiveInSeconds && this.emailVerificationIdTimeToLiveInSeconds == externalIdentifierConfiguration.emailVerificationIdTimeToLiveInSeconds && this.externalAuthenticationIdTimeToLiveInSeconds == externalIdentifierConfiguration.externalAuthenticationIdTimeToLiveInSeconds && this.identityProviderConnectionTestTimeToLiveInSeconds == externalIdentifierConfiguration.identityProviderConnectionTestTimeToLiveInSeconds && this.phoneVerificationIdTimeToLiveInSeconds == externalIdentifierConfiguration.phoneVerificationIdTimeToLiveInSeconds && this.loginIntentTimeToLiveInSeconds == externalIdentifierConfiguration.loginIntentTimeToLiveInSeconds && this.oneTimePasswordTimeToLiveInSeconds == externalIdentifierConfiguration.oneTimePasswordTimeToLiveInSeconds && this.passwordlessLoginTimeToLiveInSeconds == externalIdentifierConfiguration.passwordlessLoginTimeToLiveInSeconds && this.pendingAccountLinkTimeToLiveInSeconds == externalIdentifierConfiguration.pendingAccountLinkTimeToLiveInSeconds && this.registrationVerificationIdTimeToLiveInSeconds == externalIdentifierConfiguration.registrationVerificationIdTimeToLiveInSeconds && this.rememberOAuthScopeConsentChoiceTimeToLiveInSeconds == externalIdentifierConfiguration.rememberOAuthScopeConsentChoiceTimeToLiveInSeconds && this.samlv2AuthNRequestIdTimeToLiveInSeconds == externalIdentifierConfiguration.samlv2AuthNRequestIdTimeToLiveInSeconds && this.setupPasswordIdTimeToLiveInSeconds == externalIdentifierConfiguration.setupPasswordIdTimeToLiveInSeconds && this.trustTokenTimeToLiveInSeconds == externalIdentifierConfiguration.trustTokenTimeToLiveInSeconds && this.twoFactorIdTimeToLiveInSeconds == externalIdentifierConfiguration.twoFactorIdTimeToLiveInSeconds && this.twoFactorOneTimeCodeIdTimeToLiveInSeconds == externalIdentifierConfiguration.twoFactorOneTimeCodeIdTimeToLiveInSeconds && this.twoFactorTrustIdTimeToLiveInSeconds == externalIdentifierConfiguration.twoFactorTrustIdTimeToLiveInSeconds && this.webAuthnAuthenticationChallengeTimeToLiveInSeconds == externalIdentifierConfiguration.webAuthnAuthenticationChallengeTimeToLiveInSeconds && this.webAuthnRegistrationChallengeTimeToLiveInSeconds == externalIdentifierConfiguration.webAuthnRegistrationChallengeTimeToLiveInSeconds && 



















      
      Objects.equals(this.changePasswordIdGenerator, externalIdentifierConfiguration.changePasswordIdGenerator) && 
      Objects.equals(this.deviceUserCodeIdGenerator, externalIdentifierConfiguration.deviceUserCodeIdGenerator) && 
      Objects.equals(this.emailVerificationIdGenerator, externalIdentifierConfiguration.emailVerificationIdGenerator) && 
      Objects.equals(this.emailVerificationOneTimeCodeGenerator, externalIdentifierConfiguration.emailVerificationOneTimeCodeGenerator) && 
      Objects.equals(this.phoneVerificationIdGenerator, externalIdentifierConfiguration.phoneVerificationIdGenerator) && 
      Objects.equals(this.phoneVerificationOneTimeCodeGenerator, externalIdentifierConfiguration.phoneVerificationOneTimeCodeGenerator) && 
      Objects.equals(this.passwordlessLoginGenerator, externalIdentifierConfiguration.passwordlessLoginGenerator) && 
      Objects.equals(this.passwordlessLoginOneTimeCodeGenerator, externalIdentifierConfiguration.passwordlessLoginOneTimeCodeGenerator) && 
      Objects.equals(this.registrationVerificationIdGenerator, externalIdentifierConfiguration.registrationVerificationIdGenerator) && 
      Objects.equals(this.registrationVerificationOneTimeCodeGenerator, externalIdentifierConfiguration.registrationVerificationOneTimeCodeGenerator) && 
      Objects.equals(this.setupPasswordIdGenerator, externalIdentifierConfiguration.setupPasswordIdGenerator) && 
      Objects.equals(this.twoFactorOneTimeCodeIdGenerator, externalIdentifierConfiguration.twoFactorOneTimeCodeIdGenerator));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          Integer.valueOf(this.authorizationGrantIdTimeToLiveInSeconds), this.changePasswordIdGenerator, 
          
          Integer.valueOf(this.changePasswordIdTimeToLiveInSeconds), 
          Integer.valueOf(this.deviceCodeTimeToLiveInSeconds), this.deviceUserCodeIdGenerator, this.emailVerificationIdGenerator, 

          
          Integer.valueOf(this.emailVerificationIdTimeToLiveInSeconds), this.emailVerificationOneTimeCodeGenerator, 
          
          Integer.valueOf(this.externalAuthenticationIdTimeToLiveInSeconds), 
          Integer.valueOf(this.identityProviderConnectionTestTimeToLiveInSeconds), 
          this.phoneVerificationIdGenerator, 
          
          Integer.valueOf(this.phoneVerificationIdTimeToLiveInSeconds), this.phoneVerificationOneTimeCodeGenerator, 
          
          Integer.valueOf(this.loginIntentTimeToLiveInSeconds), 
          Integer.valueOf(this.oneTimePasswordTimeToLiveInSeconds), this.passwordlessLoginGenerator, 
          
          Integer.valueOf(this.passwordlessLoginTimeToLiveInSeconds), this.passwordlessLoginOneTimeCodeGenerator, 
          
          Integer.valueOf(this.pendingAccountLinkTimeToLiveInSeconds), this.registrationVerificationIdGenerator, 
          Integer.valueOf(this.registrationVerificationIdTimeToLiveInSeconds), this.registrationVerificationOneTimeCodeGenerator, 
          
          Integer.valueOf(this.rememberOAuthScopeConsentChoiceTimeToLiveInSeconds), 
          Integer.valueOf(this.samlv2AuthNRequestIdTimeToLiveInSeconds), this.setupPasswordIdGenerator, 
          
          Integer.valueOf(this.setupPasswordIdTimeToLiveInSeconds), 
          Integer.valueOf(this.trustTokenTimeToLiveInSeconds), 
          Integer.valueOf(this.twoFactorIdTimeToLiveInSeconds), this.twoFactorOneTimeCodeIdGenerator, 
          
          Integer.valueOf(this.twoFactorOneTimeCodeIdTimeToLiveInSeconds), 
          Integer.valueOf(this.twoFactorTrustIdTimeToLiveInSeconds), 
          Integer.valueOf(this.webAuthnAuthenticationChallengeTimeToLiveInSeconds), 
          Integer.valueOf(this.webAuthnRegistrationChallengeTimeToLiveInSeconds) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
