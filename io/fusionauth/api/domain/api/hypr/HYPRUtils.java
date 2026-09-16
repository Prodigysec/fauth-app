package io.fusionauth.api.domain.api.hypr;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class HYPRUtils {
  public static String nonce() {
    MessageDigest messageDigest;
    SecureRandom secureRandom = new SecureRandom();
    byte[] arrayOfByte1 = ("100000" + secureRandom.nextInt(900000)).getBytes();
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new RuntimeException(noSuchAlgorithmException);
    } 
    messageDigest.update(arrayOfByte1);
    byte[] arrayOfByte2 = messageDigest.digest();
    StringBuilder stringBuilder = new StringBuilder();
    for (byte b : arrayOfByte2) {
      stringBuilder.append(String.format("%02x", new Object[] { Byte.valueOf(b) }));
    } 
    return stringBuilder.toString();
  }
}
