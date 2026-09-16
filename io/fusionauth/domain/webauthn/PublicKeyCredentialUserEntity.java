package io.fusionauth.domain.webauthn;

import io.fusionauth.domain.Buildable;

public class PublicKeyCredentialUserEntity extends PublicKeyCredentialEntity implements Buildable<PublicKeyCredentialUserEntity> {
  public String displayName;
  
  public String id;
}
