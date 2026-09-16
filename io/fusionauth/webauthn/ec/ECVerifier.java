package io.fusionauth.webauthn.ec;

import io.fusionauth.domain.webauthn.CoseAlgorithmIdentifier;
import io.fusionauth.pem.domain.PEM;
import io.fusionauth.webauthn.Verifier;
import io.fusionauth.webauthn.WebAuthnException;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.util.Objects;

public class ECVerifier implements Verifier {
  private final ECPublicKey publicKey;
  
  private ECVerifier(String paramString) {
    Objects.requireNonNull(paramString);
    PEM pEM = PEM.decode(paramString);
    if (pEM.publicKey == null)
      throw new WebAuthnException("The provided PEM encoded string did not contain a public key."); 
    if (!(pEM.publicKey instanceof ECPublicKey))
      throw new WebAuthnException("Expecting a public key of type [ECPublicKey], but found [" + pEM.publicKey.getClass().getSimpleName() + "]."); 
    this.publicKey = (ECPublicKey)pEM.getPublicKey();
  }
  
  public static ECVerifier newVerifier(String paramString) {
    return new ECVerifier(paramString);
  }
  
  public void verify(CoseAlgorithmIdentifier paramCoseAlgorithmIdentifier, byte[] paramArrayOfbyte1, byte[] paramArrayOfbyte2) {
    Objects.requireNonNull(paramCoseAlgorithmIdentifier);
    Objects.requireNonNull(paramArrayOfbyte1);
    Objects.requireNonNull(paramArrayOfbyte2);
    try {
      Signature signature = Signature.getInstance(paramCoseAlgorithmIdentifier.description);
      signature.initVerify(this.publicKey);
      signature.update(paramArrayOfbyte1);
      if (!signature.verify(paramArrayOfbyte2))
        throw new WebAuthnException("Signature is invalid."); 
    } catch (InvalidKeyException|java.security.NoSuchAlgorithmException|java.security.SignatureException|SecurityException invalidKeyException) {
      throw new WebAuthnException("An unexpected exception occurred when attempting to verify the WebAuthn message", invalidKeyException);
    } 
  }
}
