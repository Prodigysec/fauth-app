package io.fusionauth.api.security;

public class PBKDF2HMACSHA256WithKeyLength512PasswordEncryptor extends BasePBKDF2HMACSHA256PasswordEncryptor {
  protected int keyLength() {
    return 512;
  }
}
