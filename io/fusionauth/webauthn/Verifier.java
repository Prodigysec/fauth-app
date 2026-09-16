package io.fusionauth.webauthn;

import io.fusionauth.domain.webauthn.CoseAlgorithmIdentifier;

public interface Verifier {
  void verify(CoseAlgorithmIdentifier paramCoseAlgorithmIdentifier, byte[] paramArrayOfbyte1, byte[] paramArrayOfbyte2);
}
