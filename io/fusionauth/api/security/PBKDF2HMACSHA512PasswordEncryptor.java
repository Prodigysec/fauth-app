package io.fusionauth.api.security;

import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF2HMACSHA512PasswordEncryptor implements PasswordEncryptor {
  public int defaultFactor() {
    return 24000;
  }
  
  public String encrypt(String paramString1, String paramString2, int paramInt) {
    SecretKeyFactory secretKeyFactory;
    SecretKey secretKey;
    if (paramInt <= 0)
      throw new IllegalArgumentException("Invalid factor value [" + paramInt + "]"); 
    try {
      secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new IllegalArgumentException("No such algorithm [PBKDF2WithHmacSHA512]", noSuchAlgorithmException);
    } 
    PBEKeySpec pBEKeySpec = new PBEKeySpec(paramString1.toCharArray(), Base64.getDecoder().decode(paramString2), paramInt, 512);
    try {
      secretKey = secretKeyFactory.generateSecret(pBEKeySpec);
    } catch (InvalidKeySpecException invalidKeySpecException) {
      throw new IllegalArgumentException("Could not generate secret key for algorithm [PBKDF2WithHmacSHA512]", invalidKeySpecException);
    } 
    byte[] arrayOfByte = secretKey.getEncoded();
    return new String(Base64.getEncoder().encode(arrayOfByte));
  }
}
