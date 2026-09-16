package io.fusionauth.api.security;

public class SaltedMD5PasswordEncryptor extends BaseDigestPasswordEncryptor {
  public int defaultFactor() {
    return 20000;
  }
  
  public String encrypt(String paramString1, String paramString2, int paramInt) {
    return encrypt("MD5", paramString1, paramString2, paramInt);
  }
}
