package io.fusionauth.api.service.security;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.inversoft.util.Pair;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.security.PasswordEncryptorLibrary;
import io.fusionauth.domain.FIPS;
import io.fusionauth.domain.UserTwoFactorConfiguration;
import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Singleton
public class DefaultRecoveryCodeHasher implements RecoveryCodeHasher {
  public static final String DEFAULT_HASHING_ALGORITHM = "salted-pbkdf2-hmac-sha256";
  
  public static final int DEFAULT_WORK_FACTOR = 24000;
  
  public static final String PLAINTEXT_SEPARATOR = "-";
  
  public static final int RECOVERY_CODE_COUNT = 10;
  
  private static final String HASH_SEPARATOR = "$";
  
  private final PasswordEncryptorLibrary passwordEncryptorLibrary;
  
  @Inject
  public DefaultRecoveryCodeHasher(PasswordEncryptorLibrary paramPasswordEncryptorLibrary) {
    this.passwordEncryptorLibrary = paramPasswordEncryptorLibrary;
  }
  
  public static List<String> generatePreviewCodes() {
    int i = FIPS.minimumRecoveryCodeLength();
    ArrayList<String> arrayList = new ArrayList(10);
    int j = i / 2;
    int k = i / 2 + i % 2;
    IntStream.range(0, 10)
      .forEach(paramInt3 -> paramList.add(SecurityTools.secureRandomAlphaNumeric(paramInt1) + "-" + SecurityTools.secureRandomAlphaNumeric(paramInt1)));
    return arrayList;
  }
  
  private static Optional<Pair<String, String>> parseStoredEntry(String paramString) {
    int i = paramString.indexOf("$");
    if (i < 0)
      return Optional.empty(); 
    return Optional.of(Pair.p(paramString.substring(0, i), paramString.substring(i + 1)));
  }
  
  public boolean compareHash(String paramString1, String paramString2) {
    return ((Boolean)parseStoredEntry(paramString2)
      .map(paramPair -> (String)paramPair.second).map(paramString2 -> {
          byte[] arrayOfByte1 = paramString1.getBytes(StandardCharsets.UTF_8);
          byte[] arrayOfByte2 = paramString2.getBytes(StandardCharsets.UTF_8);
          return Boolean.valueOf(MessageDigest.isEqual(arrayOfByte1, arrayOfByte2));
        }).orElse(Boolean.valueOf(false))).booleanValue();
  }
  
  public Optional<String> extractSalt(String paramString) {
    return parseStoredEntry(paramString).map(paramPair -> (String)paramPair.first);
  }
  
  public List<String> generateCodes() {
    return generatePreviewCodes();
  }
  
  public void hashCodes(List<String> paramList, UserTwoFactorConfiguration paramUserTwoFactorConfiguration) {
    String str1 = Optional.<String>ofNullable(paramUserTwoFactorConfiguration.recoveryCodeEncryptionScheme).orElse("salted-pbkdf2-hmac-sha256");
    Integer integer = Optional.<Integer>ofNullable(paramUserTwoFactorConfiguration.recoveryCodeWorkFactor).orElse(Integer.valueOf(24000));
    PasswordEncryptor passwordEncryptor = this.passwordEncryptorLibrary.lookup(str1);
    String str2 = passwordEncryptor.generateSalt();
    paramUserTwoFactorConfiguration.recoveryCodes.clear();
    paramUserTwoFactorConfiguration.recoveryCodes
      .addAll(paramList.stream()
        .map(paramString2 -> paramString1 + "$" + paramString1)
        .toList());
    paramUserTwoFactorConfiguration.recoveryCodeEncryptionScheme = str1;
    paramUserTwoFactorConfiguration.recoveryCodeWorkFactor = integer;
  }
  
  public Optional<String> hashOnce(String paramString1, String paramString2, String paramString3, int paramInt) {
    if (FIPS.isEnabled() && paramString1.length() < 14)
      paramString1 = paramString1 + paramString1; 
    PasswordEncryptor passwordEncryptor = this.passwordEncryptorLibrary.lookup(paramString3);
    return Optional.of(passwordEncryptor.encrypt(paramString1, paramString2, paramInt));
  }
  
  public String normalizeRecoveryCode(String paramString) {
    int i = FIPS.minimumRecoveryCodeLength();
    return (paramString.length() == i) ? (paramString.substring(0, i / 2) + "-" + paramString.substring(0, i / 2)) : paramString;
  }
}
