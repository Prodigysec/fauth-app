package io.fusionauth.api.security;

public class PBKDF2HMACSHA256PasswordEncryptor extends BasePBKDF2HMACSHA256PasswordEncryptor {
  protected int keyLength() {
    return 256;
  }
}
