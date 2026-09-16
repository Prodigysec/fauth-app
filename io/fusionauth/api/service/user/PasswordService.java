package io.fusionauth.api.service.user;

import com.inversoft.error.Errors;
import io.fusionauth.api.domain.PreviousPassword;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;

public interface PasswordService {
  public static final int BCRYPT_MAX_PASSWORD_LENGTH = 50;
  
  public static final int MAX_PASSWORD_LENGTH = 256;
  
  void hashPassword(Tenant paramTenant, User paramUser, String paramString);
  
  boolean passwordsEqual(PreviousPassword paramPreviousPassword, String paramString);
  
  boolean passwordsEqual(String paramString1, String paramString2, String paramString3, String paramString4, Integer paramInteger);
  
  void rehashPasswordOnChange(Tenant paramTenant, User paramUser, String paramString);
  
  boolean rehashPasswordOnLogin(Tenant paramTenant, User paramUser, String paramString);
  
  void rehashPasswordOnUserUpdate(Tenant paramTenant, User paramUser1, User paramUser2, String paramString);
  
  Errors validatePasswordConstraintsOnLogin(Tenant paramTenant, User paramUser, String paramString);
  
  Errors validatePasswordForChangePasswordAction(Tenant paramTenant, Application paramApplication, User paramUser1, User paramUser2, String paramString1, String paramString2, ExternalIdentifierReaderService.ValidationResult paramValidationResult, String paramString3, String paramString4, EventInfo paramEventInfo, String paramString5);
  
  Errors validatePasswordOnCreate(Tenant paramTenant, User paramUser, String paramString1, String paramString2);
  
  Errors validatePasswordOnUpdate(Tenant paramTenant, User paramUser1, User paramUser2, String paramString1, String paramString2);
}
