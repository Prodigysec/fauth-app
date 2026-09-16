package io.fusionauth.api.util;

import java.nio.charset.StandardCharsets;

public class EncoderTools {
  public static class Base32 {
    public static byte[] decode(byte[] param1ArrayOfbyte) {
      return (new org.apache.commons.codec.binary.Base32()).decode(param1ArrayOfbyte);
    }
    
    public static byte[] decode(String param1String) {
      return (new org.apache.commons.codec.binary.Base32()).decode(param1String);
    }
    
    public static byte[] encode(byte[] param1ArrayOfbyte) {
      return (new org.apache.commons.codec.binary.Base32()).encode(param1ArrayOfbyte);
    }
    
    public static String encodeToString(byte[] param1ArrayOfbyte) {
      return new String(encode(param1ArrayOfbyte), StandardCharsets.UTF_8);
    }
    
    public static boolean isValid(String param1String) {
      try {
        decode(param1String);
        return true;
      } catch (Exception exception) {
        return false;
      } 
    }
    
    public static String toBase64(String param1String) {
      return EncoderTools.Base64.encodeToString(decode(param1String));
    }
  }
  
  public static class Base64 {
    public static byte[] decode(byte[] param1ArrayOfbyte) {
      return java.util.Base64.getDecoder().decode(param1ArrayOfbyte);
    }
    
    public static byte[] decode(String param1String) {
      return java.util.Base64.getDecoder().decode(param1String.getBytes(StandardCharsets.UTF_8));
    }
    
    public static String decodeToString(String param1String) {
      return new String(java.util.Base64.getDecoder().decode(param1String.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
    
    public static byte[] encode(byte[] param1ArrayOfbyte) {
      return java.util.Base64.getEncoder().encode(param1ArrayOfbyte);
    }
    
    public static String encodeToString(byte[] param1ArrayOfbyte) {
      return new String(encode(param1ArrayOfbyte), StandardCharsets.UTF_8);
    }
    
    public static String encodeToString(String param1String) {
      return new String(encode(param1String.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
    
    public static boolean isValid(String param1String) {
      try {
        decode(param1String);
        return true;
      } catch (Exception exception) {
        return false;
      } 
    }
    
    public static String toBase32(String param1String) {
      return EncoderTools.Base32.encodeToString(decode(param1String));
    }
  }
}
