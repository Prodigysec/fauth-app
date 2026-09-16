package io.fusionauth.domain.webauthn;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;

public class AuthenticatorSelectionCriteria implements Buildable<AuthenticatorSelectionCriteria> {
  public AuthenticatorAttachment authenticatorAttachment;
  
  public Boolean requireResidentKey;
  
  public ResidentKeyRequirement residentKey;
  
  public UserVerificationRequirement userVerification;
  
  @JacksonConstructor
  public AuthenticatorSelectionCriteria() {}
  
  public AuthenticatorSelectionCriteria(AuthenticatorAttachment paramAuthenticatorAttachment, ResidentKeyRequirement paramResidentKeyRequirement, UserVerificationRequirement paramUserVerificationRequirement) {
    this(paramAuthenticatorAttachment, (paramResidentKeyRequirement == ResidentKeyRequirement.required), paramResidentKeyRequirement, paramUserVerificationRequirement);
  }
  
  public AuthenticatorSelectionCriteria(AuthenticatorAttachment paramAuthenticatorAttachment, ResidentKeyRequirement paramResidentKeyRequirement) {
    this(paramAuthenticatorAttachment, paramResidentKeyRequirement, UserVerificationRequirement.preferred);
  }
  
  private AuthenticatorSelectionCriteria(AuthenticatorAttachment paramAuthenticatorAttachment, boolean paramBoolean, ResidentKeyRequirement paramResidentKeyRequirement, UserVerificationRequirement paramUserVerificationRequirement) {
    this.authenticatorAttachment = paramAuthenticatorAttachment;
    this.requireResidentKey = Boolean.valueOf(paramBoolean);
    this.residentKey = paramResidentKeyRequirement;
    this.userVerification = paramUserVerificationRequirement;
  }
}
