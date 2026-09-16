package io.fusionauth.api.security;

import com.inversoft.lang.ArrayTools;
import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public abstract class BaseDigestPasswordEncryptor implements PasswordEncryptor {
  protected String encrypt(String paramString1, String paramString2, String paramString3, int paramInt) {
    MessageDigest messageDigest;
    if (paramInt <= 0)
      throw new IllegalArgumentException("Invalid factor value [" + paramInt + "]"); 
    try {
      messageDigest = MessageDigest.getInstance(paramString1);
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new IllegalArgumentException("No such algorithm [" + paramString1 + "]");
    } 
    byte[] arrayOfByte1 = Base64.getDecoder().decode(paramString3.getBytes(StandardCharsets.UTF_8));
    byte[] arrayOfByte2 = ArrayTools.join(paramString2.getBytes(StandardCharsets.UTF_8), arrayOfByte1);
    for (byte b = 0; b < paramInt; b++)
      arrayOfByte2 = messageDigest.digest(arrayOfByte2); 
    return new String(Base64.getEncoder().encode(arrayOfByte2));
  }
}
