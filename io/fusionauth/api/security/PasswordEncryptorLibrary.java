package io.fusionauth.api.security;

import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import java.util.List;

public interface PasswordEncryptorLibrary {
  String getEncryptorDisplayName(String paramString);
  
  List<String> getSchemeNames();
  
  PasswordEncryptor lookup(String paramString) throws UnknownEncryptionSchemeException;
  
  boolean validateFactor(String paramString1, String paramString2, Integer paramInteger);
  
  boolean validateScheme(String paramString);
}
