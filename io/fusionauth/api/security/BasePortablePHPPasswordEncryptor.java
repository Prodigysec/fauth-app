package io.fusionauth.api.security;

import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.regex.Pattern;

public abstract class BasePortablePHPPasswordEncryptor implements PasswordEncryptor {
  protected static final char[] BASE_64_TABLE = new char[] { 
      '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', 
      '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 
      'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 
      'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 
      'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 
      'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 
      'w', 'x', 'y', 'z' };
  
  private static final Pattern PortablePHPSaltPattern = Pattern.compile("^[./A-Za-z0-9]+=*$");
  
  public String generateSalt() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] arrayOfByte = new byte[13];
    secureRandom.nextBytes(arrayOfByte);
    return base64Encode(arrayOfByte, 6);
  }
  
  public boolean validateSalt(String paramString) {
    return PortablePHPSaltPattern.matcher(paramString).matches();
  }
  
  protected String base64Encode(byte[] paramArrayOfbyte, int paramInt) {
    StringBuilder stringBuilder = new StringBuilder();
    byte b = 0;
    if (paramArrayOfbyte.length < paramInt) {
      byte[] arrayOfByte = new byte[paramInt];
      System.arraycopy(paramArrayOfbyte, 0, arrayOfByte, 0, paramArrayOfbyte.length);
      Arrays.fill(arrayOfByte, paramArrayOfbyte.length, paramInt - 1, (byte)0);
      paramArrayOfbyte = arrayOfByte;
    } 
    do {
      long l = Byte.toUnsignedLong(paramArrayOfbyte[b++]);
      stringBuilder.append(BASE_64_TABLE[(int)l & 0x3F]);
      if (b < paramInt)
        l |= Byte.toUnsignedLong(paramArrayOfbyte[b]) << 8L; 
      stringBuilder.append(BASE_64_TABLE[(int)(l >> 6L) & 0x3F]);
      if (b++ >= paramInt)
        break; 
      if (b < paramInt)
        l |= Byte.toUnsignedLong(paramArrayOfbyte[b]) << 16L; 
      stringBuilder.append(BASE_64_TABLE[(int)(l >> 12L) & 0x3F]);
      if (b++ >= paramInt)
        break; 
      stringBuilder.append(BASE_64_TABLE[(int)(l >> 18L) & 0x3F]);
    } while (b < paramInt);
    return stringBuilder.toString();
  }
}
