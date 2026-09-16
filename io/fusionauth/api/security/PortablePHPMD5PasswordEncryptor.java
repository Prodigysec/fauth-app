package io.fusionauth.api.security;

import com.inversoft.lang.ArrayTools;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PortablePHPMD5PasswordEncryptor extends BasePortablePHPPasswordEncryptor {
  public int defaultFactor() {
    return 2048;
  }
  
  public String encrypt(String paramString1, String paramString2, int paramInt) {
    MessageDigest messageDigest;
    if (paramInt <= 0)
      throw new IllegalArgumentException("Invalid factor value [" + paramInt + "]"); 
    try {
      messageDigest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new IllegalArgumentException("No such algorithm [MD5]");
    } 
    byte[] arrayOfByte1 = paramString1.getBytes(StandardCharsets.UTF_8);
    byte[] arrayOfByte2 = messageDigest.digest(ArrayTools.join(paramString2.getBytes(StandardCharsets.UTF_8), arrayOfByte1));
    for (byte b = 0; b < paramInt; b++) {
      byte[] arrayOfByte = ArrayTools.join(arrayOfByte2, arrayOfByte1);
      arrayOfByte2 = messageDigest.digest(arrayOfByte);
    } 
    return base64Encode(arrayOfByte2, 16);
  }
}
