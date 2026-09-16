package io.fusionauth.api.service.security;

import io.fusionauth.domain.UserTwoFactorConfiguration;
import java.util.List;
import java.util.Optional;

public interface RecoveryCodeHasher {
  boolean compareHash(String paramString1, String paramString2);
  
  Optional<String> extractSalt(String paramString);
  
  List<String> generateCodes();
  
  void hashCodes(List<String> paramList, UserTwoFactorConfiguration paramUserTwoFactorConfiguration);
  
  Optional<String> hashOnce(String paramString1, String paramString2, String paramString3, int paramInt);
  
  String normalizeRecoveryCode(String paramString);
}
