package io.fusionauth.api.security;

import com.google.inject.Inject;
import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultPasswordEncryptorLibrary implements PasswordEncryptorLibrary {
  private static final Map<String, String> Deprecated = new HashMap<>();
  
  private final Map<String, PasswordEncryptor> library;
  
  @Inject
  public DefaultPasswordEncryptorLibrary(Map<String, PasswordEncryptor> paramMap) {
    this.library = paramMap;
  }
  
  public String getEncryptorDisplayName(String paramString) {
    PasswordEncryptor passwordEncryptor = this.library.get(paramString);
    if (passwordEncryptor == null)
      return paramString; 
    return (passwordEncryptor.pluginDisplayName() != null) ? passwordEncryptor.pluginDisplayName() : paramString;
  }
  
  public List<String> getSchemeNames() {
    return new ArrayList<>(this.library.keySet());
  }
  
  public PasswordEncryptor lookup(String paramString) {
    PasswordEncryptor passwordEncryptor = this.library.get(Deprecated.getOrDefault(paramString, paramString));
    if (passwordEncryptor == null)
      throw new UnknownEncryptionSchemeException("Unknown scheme [" + paramString + "]."); 
    return passwordEncryptor;
  }
  
  public boolean validateFactor(String paramString1, String paramString2, Integer paramInteger) {
    if (paramString1 == null)
      paramString1 = paramString2; 
    if (paramString1.equals("bcrypt"))
      return (paramInteger.intValue() >= 4 && paramInteger.intValue() <= 30); 
    return (paramInteger.intValue() >= 1 && paramInteger.intValue() <= Integer.MAX_VALUE);
  }
  
  public boolean validateScheme(String paramString) {
    return this.library.containsKey(paramString);
  }
  
  static {
    Deprecated.put("fusionauth", "salted-pbkdf2-hmac-sha256");
    Deprecated.put("django", "salted-pbkdf2-hmac-sha256");
  }
}
