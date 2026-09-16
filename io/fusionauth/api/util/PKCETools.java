package io.fusionauth.api.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

public class PKCETools {
  private static final Pattern PKCE_CODE_PATTERN = Pattern.compile("^([a-zA-Z]|[0-9]|-|_|\\.|~){43,128}$");
  
  public static String generateCodeChallenge(String paramString) {
    try {
      byte[] arrayOfByte1 = paramString.getBytes(StandardCharsets.UTF_8);
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      messageDigest.update(arrayOfByte1);
      byte[] arrayOfByte2 = messageDigest.digest();
      return Base64.getUrlEncoder().withoutPadding().encodeToString(arrayOfByte2);
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new RuntimeException(noSuchAlgorithmException);
    } 
  }
  
  public static String generateCodeVerifier(int paramInt) {
    if (paramInt < 32 || paramInt > 96)
      throw new IllegalStateException("Invalid length. The code verifier must be greater than or equal to 43 and less than or equal to 128 characters in length once Base64 encoded. Use a byte length of greater than or equal to 32 or a less than or equal to 96."); 
    SecureRandom secureRandom = new SecureRandom();
    byte[] arrayOfByte = new byte[paramInt];
    secureRandom.nextBytes(arrayOfByte);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(arrayOfByte);
  }
  
  public static String generateCodeVerifier() {
    return generateCodeVerifier(32);
  }
  
  public static boolean validateCodeVerifier(String paramString) {
    return PKCE_CODE_PATTERN.matcher(paramString).matches();
  }
}
