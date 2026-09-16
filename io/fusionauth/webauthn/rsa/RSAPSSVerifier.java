package io.fusionauth.webauthn.rsa;

import io.fusionauth.domain.webauthn.CoseAlgorithmIdentifier;
import io.fusionauth.pem.domain.PEM;
import io.fusionauth.webauthn.Verifier;
import io.fusionauth.webauthn.WebAuthnException;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Objects;

public class RSAPSSVerifier implements Verifier {
  private final RSAPublicKey publicKey;
  
  private RSAPSSVerifier(String paramString) {
    Objects.requireNonNull(paramString);
    PEM pEM = PEM.decode(paramString);
    if (pEM.publicKey == null)
      throw new WebAuthnException("The provided PEM encoded string did not contain a public key."); 
    if (!(pEM.publicKey instanceof RSAPublicKey))
      throw new WebAuthnException("Expecting a public key of type [RSAPublicKey], but found [" + pEM.publicKey.getClass().getSimpleName() + "]."); 
    this.publicKey = (RSAPublicKey)pEM.getPublicKey();
  }
  
  public static RSAPSSVerifier newVerifier(String paramString) {
    return new RSAPSSVerifier(paramString);
  }
  
  public void verify(CoseAlgorithmIdentifier paramCoseAlgorithmIdentifier, byte[] paramArrayOfbyte1, byte[] paramArrayOfbyte2) {
    Objects.requireNonNull(paramCoseAlgorithmIdentifier);
    Objects.requireNonNull(paramArrayOfbyte1);
    Objects.requireNonNull(paramArrayOfbyte2);
    try {
      Signature signature = Signature.getInstance("RSASSA-PSS");
      signature.setParameter(new PSSParameterSpec(paramCoseAlgorithmIdentifier.description, "MGF1", new MGF1ParameterSpec(paramCoseAlgorithmIdentifier.description), paramCoseAlgorithmIdentifier.getSaltLength(), 1));
      signature.initVerify(this.publicKey);
      signature.update(paramArrayOfbyte1);
      if (!signature.verify(paramArrayOfbyte2))
        throw new WebAuthnException("Signature is invalid."); 
    } catch (InvalidKeyException|java.security.NoSuchAlgorithmException|java.security.SignatureException|SecurityException|java.security.InvalidAlgorithmParameterException invalidKeyException) {
      throw new WebAuthnException("An unexpected exception occurred when attempting to verify the WebAuthn message", invalidKeyException);
    } 
  }
}
