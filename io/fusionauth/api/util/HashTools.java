package io.fusionauth.api.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashTools {
  public static String sha256(String paramString) {
    return sha256(paramString.getBytes(StandardCharsets.UTF_8));
  }
  
  public static String sha256(byte[] paramArrayOfbyte) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      return EncoderTools.Base64.encodeToString(messageDigest.digest(paramArrayOfbyte));
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new IllegalStateException("Unexpected, SHA-256 is not available.");
    } 
  }
  
  public static String sha256urlEncoding(String paramString) {
    return sha256urlEncoding(paramString.getBytes(StandardCharsets.UTF_8));
  }
  
  public static String sha256urlEncoding(byte[] paramArrayOfbyte) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      return Base64.getUrlEncoder().withoutPadding().encodeToString(messageDigest.digest(paramArrayOfbyte));
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new IllegalStateException("Unexpected, SHA-256 is not available.");
    } 
  }
}
