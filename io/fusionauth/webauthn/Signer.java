package io.fusionauth.webauthn;

public interface Signer {
  byte[] sign(byte[] paramArrayOfbyte);
}
