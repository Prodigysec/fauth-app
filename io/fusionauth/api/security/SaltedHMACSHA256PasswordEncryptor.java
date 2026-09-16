package io.fusionauth.api.security;

import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SaltedHMACSHA256PasswordEncryptor implements PasswordEncryptor {
  public int defaultFactor() {
    return 1;
  }
  
  public String encrypt(String paramString1, String paramString2, int paramInt) {
    Mac mac;
    SecretKeySpec secretKeySpec;
    byte[] arrayOfByte;
    try {
      mac = Mac.getInstance("HmacSHA256");
      secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(paramString2), "HmacSHA256");
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new IllegalArgumentException("No such algorithm [HmacSHA256]");
    } 
    try {
      mac.init(secretKeySpec);
      arrayOfByte = mac.doFinal(paramString1.getBytes(StandardCharsets.UTF_8));
    } catch (InvalidKeyException invalidKeyException) {
      throw new IllegalArgumentException("Could not generate secret key for algorithm [HmacSHA256]");
    } 
    return new String(Base64.getEncoder().encode(arrayOfByte));
  }
}
