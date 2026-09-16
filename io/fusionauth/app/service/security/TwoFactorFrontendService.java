package io.fusionauth.app.service.security;

import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import java.util.List;

public interface TwoFactorFrontendService {
  List<String> availableMethodsToConfigureForUser(Tenant paramTenant, User paramUser);
  
  List<String> availableMethodsToUseAtLogin(Tenant paramTenant, User paramUser);
  
  ExternalIdentifierReaderService.ValidationResult verifyTwoFactorId(Tenant paramTenant, String paramString);
}
