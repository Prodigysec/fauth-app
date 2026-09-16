package io.fusionauth.api.domain.webauthn;

import io.fusionauth.domain.Buildable;

public class AuthenticatorData implements Buildable<AuthenticatorData> {
  private static final int CREDENTIAL_DATA = 64;
  
  private static final int EXTENSION_DATA = 128;
  
  private static final int USER_PRESENT = 1;
  
  private static final int USER_VERIFIED = 4;
  
  public AttestedCredentialData attestedCredentialData;
  
  public byte flags;
  
  public byte[] rpIdHash;
  
  public int signCount;
  
  public boolean hasCredentialData() {
    return ((this.flags & 0x40) != 0);
  }
  
  public boolean hasExtensionData() {
    return ((this.flags & 0x80) != 0);
  }
  
  public boolean userPresent() {
    return ((this.flags & 0x1) != 0);
  }
  
  public boolean userVerified() {
    return ((this.flags & 0x4) != 0);
  }
}
