package io.fusionauth.api.service.user;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.PreviousPassword;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.domain.ZonedDateTimeWrapper;
import io.fusionauth.api.domain.api.reactor.BreachResult;
import io.fusionauth.api.security.PasswordEncryptorLibrary;
import io.fusionauth.api.security.PasswordValidator;
import io.fusionauth.api.security.guice.SecurityModule;
import io.fusionauth.api.service.mfa.MFAService;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.api.service.risk.CompositeRisk;
import io.fusionauth.api.service.risk.RiskSignalContext;
import io.fusionauth.api.service.risk.RiskSignalService;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.MultiFactorAction;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPasswordService implements PasswordService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultPasswordService.class);
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final MFAService mfaService;
  
  private final PasswordEncryptorLibrary passwordEncryptorLibrary;
  
  private final ReactorService reactorService;
  
  private final RiskSignalService riskSignalService;
  
  private final UserMapper userMapper;
  
  @Inject
  public DefaultPasswordService(ExternalIdentifierReaderService paramExternalIdentifierReaderService, PasswordEncryptorLibrary paramPasswordEncryptorLibrary, ReactorService paramReactorService, RiskSignalService paramRiskSignalService, UserMapper paramUserMapper, MFAService paramMFAService) {
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.passwordEncryptorLibrary = paramPasswordEncryptorLibrary;
    this.reactorService = paramReactorService;
    this.riskSignalService = paramRiskSignalService;
    this.userMapper = paramUserMapper;
    this.mfaService = paramMFAService;
  }
  
  private static String hashPassword(PasswordEncryptor paramPasswordEncryptor, String paramString1, String paramString2, int paramInt, String paramString3) {
    try {
      return paramPasswordEncryptor.encrypt(paramString1, paramString2, paramInt);
    } catch (Throwable throwable) {
      logger.debug("Unable to calculate a hashed password. Scheme [" + paramString3 + "] Factor [" + paramInt + "]", throwable);
      return null;
    } 
  }
  
  public void hashPassword(Tenant paramTenant, User paramUser, String paramString) {
    if (paramString == null) {
      PasswordEncryptor passwordEncryptor1 = this.passwordEncryptorLibrary.lookup("null-password");
      paramUser.salt = passwordEncryptor1.generateSalt();
      paramUser.factor = null;
      paramUser.encryptionScheme = null;
      return;
    } 
    if (paramUser.encryptionScheme == null)
      paramUser.encryptionScheme = paramTenant.passwordEncryptionConfiguration.encryptionScheme; 
    PasswordEncryptor passwordEncryptor = this.passwordEncryptorLibrary.lookup(paramUser.encryptionScheme);
    paramUser.salt = passwordEncryptor.generateSalt();
    if (paramUser.factor == null)
      if (paramUser.encryptionScheme.equals(paramTenant.passwordEncryptionConfiguration.encryptionScheme)) {
        paramUser.factor = Integer.valueOf(paramTenant.passwordEncryptionConfiguration.encryptionSchemeFactor);
      } else {
        paramUser.factor = Integer.valueOf(passwordEncryptor.defaultFactor());
      }  
    paramUser.password = hashPassword(passwordEncryptor, paramString, paramUser.salt, paramUser.factor.intValue(), paramUser.encryptionScheme);
    paramUser.passwordLastUpdateInstant = ZonedDateTimeWrapper.now(ZoneOffset.UTC);
  }
  
  public boolean passwordsEqual(String paramString1, String paramString2, String paramString3, String paramString4, Integer paramInteger) {
    PasswordEncryptor passwordEncryptor;
    if (paramString2 == null) {
      passwordEncryptor = this.passwordEncryptorLibrary.lookup("null-password");
      paramString2 = "*";
      paramInteger = Integer.valueOf(passwordEncryptor.defaultFactor());
    } else {
      passwordEncryptor = this.passwordEncryptorLibrary.lookup(paramString4);
    } 
    if (paramString1.length() > 256)
      return false; 
    String str = hashPassword(passwordEncryptor, paramString1, paramString3, paramInteger.intValue(), paramString4);
    if (str == null)
      return false; 
    return MessageDigest.isEqual(str.getBytes(StandardCharsets.UTF_8), paramString2.getBytes(StandardCharsets.UTF_8));
  }
  
  public boolean passwordsEqual(PreviousPassword paramPreviousPassword, String paramString) {
    return passwordsEqual(paramString, paramPreviousPassword.password, paramPreviousPassword.salt, paramPreviousPassword.encryptionScheme, paramPreviousPassword.factor);
  }
  
  public void rehashPasswordOnChange(Tenant paramTenant, User paramUser, String paramString) {
    boolean bool1 = (paramUser.encryptionScheme == null || (paramTenant.passwordEncryptionConfiguration.modifyEncryptionSchemeOnLogin && !paramUser.encryptionScheme.equals(paramTenant.passwordEncryptionConfiguration.encryptionScheme))) ? true : false;
    boolean bool2 = (paramUser.encryptionScheme == null || (paramTenant.passwordEncryptionConfiguration.modifyEncryptionSchemeOnLogin && !paramUser.factor.equals(Integer.valueOf(paramTenant.passwordEncryptionConfiguration.encryptionSchemeFactor)))) ? true : false;
    paramUser
      
      .encryptionScheme = bool1 ? paramTenant.passwordEncryptionConfiguration.encryptionScheme : paramUser.encryptionScheme;
    paramUser.factor = Integer.valueOf(bool2 ? 
        paramTenant.passwordEncryptionConfiguration.encryptionSchemeFactor : 
        paramUser.factor.intValue());
    PasswordEncryptor passwordEncryptor = this.passwordEncryptorLibrary.lookup(paramUser.encryptionScheme);
    paramUser.salt = passwordEncryptor.generateSalt();
    if (paramUser.factor == null)
      paramUser.factor = Integer.valueOf(passwordEncryptor.defaultFactor()); 
    paramUser.password = hashPassword(passwordEncryptor, paramString, paramUser.salt, paramUser.factor.intValue(), paramUser.encryptionScheme);
    paramUser.passwordLastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
  }
  
  public boolean rehashPasswordOnLogin(Tenant paramTenant, User paramUser, String paramString) {
    if (paramTenant.passwordEncryptionConfiguration.modifyEncryptionSchemeOnLogin) {
      String str = paramTenant.passwordEncryptionConfiguration.encryptionScheme;
      int i = paramTenant.passwordEncryptionConfiguration.encryptionSchemeFactor;
      if (!paramUser.encryptionScheme.equals(str) || paramUser.factor.intValue() != i) {
        PasswordEncryptor passwordEncryptor = this.passwordEncryptorLibrary.lookup(str);
        paramUser.encryptionScheme = str;
        paramUser.factor = Integer.valueOf(i);
        paramUser.salt = passwordEncryptor.generateSalt();
        paramUser.password = hashPassword(passwordEncryptor, paramString, paramUser.salt, paramUser.factor.intValue(), paramUser.encryptionScheme);
        return true;
      } 
    } 
    return false;
  }
  
  public void rehashPasswordOnUserUpdate(Tenant paramTenant, User paramUser1, User paramUser2, String paramString) {
    String str = paramUser2.encryptionScheme;
    paramUser1.encryptionScheme = (paramUser1.encryptionScheme != null) ? paramUser1.encryptionScheme : paramTenant.passwordEncryptionConfiguration.encryptionScheme;
    paramUser2
      
      .encryptionScheme = (paramUser2.encryptionScheme != null) ? paramUser2.encryptionScheme : (paramTenant.passwordEncryptionConfiguration.modifyEncryptionSchemeOnLogin ? paramTenant.passwordEncryptionConfiguration.encryptionScheme : paramUser1.encryptionScheme);
    PasswordEncryptor passwordEncryptor = this.passwordEncryptorLibrary.lookup(paramUser2.encryptionScheme);
    paramUser2.salt = passwordEncryptor.generateSalt();
    if (paramUser2.factor == null)
      if (paramTenant.passwordEncryptionConfiguration.modifyEncryptionSchemeOnLogin && paramUser2.encryptionScheme.equals(paramTenant.passwordEncryptionConfiguration.encryptionScheme)) {
        paramUser2.factor = Integer.valueOf(paramTenant.passwordEncryptionConfiguration.encryptionSchemeFactor);
      } else if (str != null) {
        paramUser2.factor = Integer.valueOf(passwordEncryptor.defaultFactor());
      } else {
        paramUser2.factor = Integer.valueOf((paramUser1.factor != null) ? paramUser1.factor.intValue() : passwordEncryptor.defaultFactor());
      }  
    paramUser2.password = hashPassword(passwordEncryptor, paramString, paramUser2.salt, paramUser2.factor.intValue(), paramUser2.encryptionScheme);
    paramUser2.passwordLastUpdateInstant = ZonedDateTimeWrapper.now(ZoneOffset.UTC);
  }
  
  public Errors validatePasswordConstraintsOnLogin(Tenant paramTenant, User paramUser, String paramString) {
    String str = "password";
    return (new PasswordValidator(paramTenant.passwordValidationRules, paramUser)).validate(paramString, str);
  }
  
  public Errors validatePasswordForChangePasswordAction(Tenant paramTenant, Application paramApplication, User paramUser1, User paramUser2, String paramString1, String paramString2, ExternalIdentifierReaderService.ValidationResult paramValidationResult, String paramString3, String paramString4, EventInfo paramEventInfo, String paramString5) {
    String str1 = (paramEventInfo == null) ? null : paramEventInfo.ipAddress;
    String str2 = (paramEventInfo == null) ? null : paramEventInfo.userAgent;
    RiskSignalContext riskSignalContext = new RiskSignalContext(paramUser2, str1, str2, null, null, false, paramTenant.clientRiskConfiguration);
    CompositeRisk compositeRisk = this.riskSignalService.computeClientRisk(riskSignalContext);
    MFAService.ChallengeResult challengeResult = this.mfaService.determineChallengeRequired(MultiFactorAction.changePassword, paramTenant, paramUser1, paramApplication, null, paramEventInfo, compositeRisk, paramString5, null, true);
    if (challengeResult.required() && paramUser1.twoFactorEnabled()) {
      boolean bool = (paramValidationResult != null && paramValidationResult.id != null && paramValidationResult.id.getAttributeAsBoolean("implicitTrust")) ? true : false;
      if (!bool) {
        ExternalIdentifier externalIdentifier = (paramString3 != null) ? (this.externalIdentifierReader.validate(paramTenant, paramString3, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TrustToken })).id : null;
        if (externalIdentifier != null && externalIdentifier.userId
          
          .equals(paramUser1.id)) {
          String str = externalIdentifier.getAttribute("trustChallenge");
          if (str != null) {
            bool = (paramString4 != null && paramString4.equals(str)) ? true : false;
          } else {
            bool = true;
          } 
        } 
        if (!bool) {
          Errors errors = new Errors();
          errors.addGeneralError("[TrustTokenRequired]", null, new Object[0]);
          return errors;
        } 
      } 
    } 
    return validatePasswordOnUpdate(paramTenant, paramUser1, paramUser2, paramString1, paramString2);
  }
  
  public Errors validatePasswordOnCreate(Tenant paramTenant, User paramUser, String paramString1, String paramString2) {
    Errors errors = (new PasswordValidator(paramTenant.passwordValidationRules, paramUser)).validate(paramString2, paramString1);
    if (!errors.empty())
      return errors; 
    validateBcryptMaxLength(paramUser.encryptionScheme, paramString1, paramString2, errors);
    if (!errors.empty())
      return errors; 
    handleBreachDetectionDuringCreateOrUpdate(paramTenant, paramUser, paramString2, paramString1, errors);
    return errors;
  }
  
  public Errors validatePasswordOnUpdate(Tenant paramTenant, User paramUser1, User paramUser2, String paramString1, String paramString2) {
    if (paramString2 == null)
      return new Errors(); 
    Errors errors = (new PasswordValidator(paramTenant.passwordValidationRules, paramUser2)).validate(paramString2, paramString1);
    if (!errors.empty())
      return errors; 
    if (!paramTenant.passwordEncryptionConfiguration.modifyEncryptionSchemeOnLogin) {
      String str = (paramUser2.encryptionScheme != null) ? paramUser2.encryptionScheme : paramUser1.encryptionScheme;
      validateBcryptMaxLength(str, paramString1, paramString2, errors);
      if (!errors.empty())
        return errors; 
    } 
    validateMinAgeAndPreviouslyUsed(paramTenant, paramUser1, paramString2, errors, paramString1);
    if (!errors.empty())
      return errors; 
    handleBreachDetectionDuringCreateOrUpdate(paramTenant, paramUser2, paramString2, paramString1, errors);
    return errors;
  }
  
  private void handleBreachDetectionDuringCreateOrUpdate(Tenant paramTenant, User paramUser, String paramString1, String paramString2, Errors paramErrors) {
    if (paramString1 == null || paramString1.length() == 0) {
      logger.warn("Skipping breach detection, password was not provided.");
      return;
    } 
    if (paramTenant.passwordValidationRules.breachDetection.enabled) {
      BreachResult breachResult = this.reactorService.retrieveBreachResultForChange(paramUser, paramString1);
      if (breachResult != null) {
        this.reactorService.updateBreachMetrics(paramTenant.id, breachResult);
        paramUser.breachedPasswordLastCheckedInstant = ZonedDateTime.now(ZoneOffset.UTC);
        paramUser.breachedPasswordStatus = breachResult.match;
        if (breachResult.isBreached(paramTenant.passwordValidationRules.breachDetection))
          paramErrors.addFieldError(paramString2, "[breached" + String.valueOf(breachResult.match) + "]" + paramString2, null, new Object[0]); 
      } 
    } 
  }
  
  private void validateBcryptMaxLength(String paramString1, String paramString2, String paramString3, Errors paramErrors) {
    if (SecurityModule.Bcrypt.equals(paramString1)) {
      Objects.requireNonNull(paramErrors);
      (new Validator()).maxLength(paramString3, 50, paramString2, new Object[] { Integer.valueOf(50) }).done(paramErrors::add);
    } 
  }
  
  private void validateMinAgeAndPreviouslyUsed(Tenant paramTenant, User paramUser, String paramString1, Errors paramErrors, String paramString2) {
    if (paramTenant.minimumPasswordAge.enabled && paramUser.passwordLastUpdateInstant != null) {
      ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
      int i = paramTenant.minimumPasswordAge.seconds;
      if (paramUser.passwordLastUpdateInstant.plusSeconds(i).isAfter(zonedDateTime)) {
        paramErrors.addFieldError(paramString2, "[tooYoung]" + paramString2, null, new Object[] { Integer.valueOf(i) });
        return;
      } 
    } 
    if (paramTenant.passwordValidationRules.rememberPreviousPasswords.enabled) {
      List<PreviousPassword> list1 = this.userMapper.retrievePreviousPasswordsByUserId(paramUser.id);
      if (list1.isEmpty())
        return; 
      list1 = list1.stream().sorted(Collections.reverseOrder(Comparator.comparing(paramPreviousPassword -> paramPreviousPassword.insertInstant))).toList();
      int i = paramTenant.passwordValidationRules.rememberPreviousPasswords.count;
      if (list1.size() > i) {
        List<PreviousPassword> list = list1.subList(i, list1.size());
        List<?> list3 = (List)list.stream().map(paramPreviousPassword -> paramPreviousPassword.insertInstant).collect(Collectors.toList());
        int j = MapperTools.safeDelete(5000, list3, paramList -> Integer.valueOf(this.userMapper.deletePreviousPasswordsByInsertInstants(paramUser.id, paramList)));
        if (j != list.size()) {
          String str = list3.stream().map(paramZonedDateTime -> String.valueOf(paramZonedDateTime.toInstant().toEpochMilli())).collect(Collectors.joining(", "));
          logger.error("Failed to delete previous passwords for user [{}] with instants instants of [{}].", paramUser.id, str);
        } 
      } 
      List<PreviousPassword> list2 = list1.subList(0, Math.min(i, list1.size()));
      if (list2.stream().anyMatch(paramPreviousPassword -> passwordsEqual(paramPreviousPassword, paramString)))
        paramErrors.addFieldError(paramString2, "[previouslyUsed]" + paramString2, null, new Object[] { Integer.valueOf(i) }); 
    } 
  }
}
