package io.fusionauth.api.service.user;

import com.google.inject.Inject;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.ExternalIdentifierMapper;
import io.fusionauth.api.domain.IdentityExternalIdHelper;
import io.fusionauth.api.service.ServiceUnavailableException;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.PasswordlessStrategy;
import io.fusionauth.domain.SecureGeneratorConfiguration;
import io.fusionauth.domain.SecureGeneratorType;
import io.fusionauth.domain.Tenant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class DefaultExternalIdentifierService implements ExternalIdentifierService {
  private static final SecureGeneratorConfiguration defaultConfig = new SecureGeneratorConfiguration(32, SecureGeneratorType.randomBytes);
  
  private final ExternalIdentifierMapper externalIdentifierMapper;
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  @Inject
  public DefaultExternalIdentifierService(ExternalIdentifierMapper paramExternalIdentifierMapper, ExternalIdentifierReaderService paramExternalIdentifierReaderService) {
    this.externalIdentifierMapper = paramExternalIdentifierMapper;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
  }
  
  public String createAuthorizationCode(Tenant paramTenant, UUID paramUUID, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, null, paramUUID, null, ExternalIdentifier.ExternalIdType.AuthorizationCode, false, false, paramExternalIdData, null)).id;
  }
  
  public String createAuthorizedSupportId(Tenant paramTenant, ExternalIdentifier.ExternalIdData paramExternalIdData, ZonedDateTime paramZonedDateTime) {
    return (createExternalIdentifier(paramTenant, null, null, null, ExternalIdentifier.ExternalIdType.AuthorizedSupportId, true, false, paramExternalIdData, paramZonedDateTime)).id;
  }
  
  public String createChangePassword(Tenant paramTenant, UUID paramUUID, String paramString, boolean paramBoolean, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, paramString, paramUUID, null, ExternalIdentifier.ExternalIdType.ChangePassword, true, paramBoolean, paramExternalIdData, null)).id;
  }
  
  public String createDeviceCode(Tenant paramTenant, UUID paramUUID, String paramString, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, paramString, null, paramUUID, ExternalIdentifier.ExternalIdType.DeviceCode, false, false, paramExternalIdData, null)).id;
  }
  
  public String createDeviceUserCode(Tenant paramTenant, UUID paramUUID, String paramString1, String paramString2, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, paramString2, null, paramUUID, ExternalIdentifier.ExternalIdType.DeviceUserCode, false, false, paramExternalIdData, null)).id;
  }
  
  public String createExternalAuthentication(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, null, paramUUID1, paramUUID2, ExternalIdentifier.ExternalIdType.ExternalAuthentication, false, false, paramExternalIdData, null)).id;
  }
  
  public ExternalIdentifier createExternalIdentifier(Tenant paramTenant, String paramString, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdType paramExternalIdType, boolean paramBoolean1, boolean paramBoolean2, ExternalIdentifier.ExternalIdData paramExternalIdData, ZonedDateTime paramZonedDateTime) {
    if (paramBoolean1)
      this.externalIdentifierMapper.deleteByUserIdApplicationIdAndType(paramUUID1, paramUUID2, paramExternalIdType); 
    if (paramString == null)
      paramString = generateExternalId(paramTenant, paramExternalIdType); 
    String str = paramString;
    ExternalIdentifier externalIdentifier = (new ExternalIdentifier()).with(paramExternalIdentifier -> paramExternalIdentifier.applicationId = paramUUID).with(paramExternalIdentifier -> paramExternalIdentifier.data = paramExternalIdData).with(paramExternalIdentifier -> paramExternalIdentifier.expirationInstant = paramZonedDateTime).with(paramExternalIdentifier -> paramExternalIdentifier.id = paramString).with(paramExternalIdentifier -> paramExternalIdentifier.insertInstant = ZonedDateTime.now(ZoneOffset.UTC)).with(paramExternalIdentifier -> paramExternalIdentifier.tenantId = paramTenant.id).with(paramExternalIdentifier -> paramExternalIdentifier.type = paramExternalIdType).with(paramExternalIdentifier -> paramExternalIdentifier.userId = paramUUID);
    if (paramBoolean2)
      externalIdentifier.setSentToUser(); 
    this.externalIdentifierMapper.create(externalIdentifier);
    return externalIdentifier;
  }
  
  public String createIdentityProviderConnectionTest(Tenant paramTenant, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, null, null, null, ExternalIdentifier.ExternalIdType.IdentityProviderConnectionTest, false, false, paramExternalIdData, null)).id;
  }
  
  public ExternalIdentifier createIdentityVerification(Tenant paramTenant, ExternalIdentifier.ExternalIdType paramExternalIdType, ExternalIdentifier.ExternalIdData paramExternalIdData, UUID paramUUID1, UUID paramUUID2) {
    IdentityType identityType = IdentityExternalIdHelper.getLoginIdentityType(paramExternalIdType);
    deleteIdentityVerificationsByUserAndIdentityType(paramUUID2, paramExternalIdData
        .getAttribute("loginId"), identityType);
    return createExternalIdentifier(paramTenant, null, paramUUID2, paramUUID1, paramExternalIdType, false, false, paramExternalIdData, null);
  }
  
  public String createLoginIntent(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, null, paramUUID1, paramUUID2, ExternalIdentifier.ExternalIdType.LoginIntent, true, false, paramExternalIdData, null)).id;
  }
  
  public String createOneTimePassword(Tenant paramTenant, UUID paramUUID, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, null, paramUUID, null, ExternalIdentifier.ExternalIdType.OneTimePassword, true, false, paramExternalIdData, null)).id;
  }
  
  public ExternalIdentifier createPasswordlessLogin(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, boolean paramBoolean, PasswordlessStrategy paramPasswordlessStrategy, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    if (PasswordlessStrategy.FormField.equals(paramPasswordlessStrategy))
      paramExternalIdData.setAttribute("otp", generateExternalId(paramTenant, ExternalIdentifier.ExternalIdType.PasswordlessLoginOneTimeCode)); 
    return createExternalIdentifier(paramTenant, null, paramUUID1, paramUUID2, ExternalIdentifier.ExternalIdType.PasswordlessLogin, true, paramBoolean, paramExternalIdData, null);
  }
  
  public String createPendingIdPLinkId(Tenant paramTenant, UUID paramUUID, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, null, null, paramUUID, ExternalIdentifier.ExternalIdType.PendingIdPLinkId, false, false, paramExternalIdData, null)).id;
  }
  
  public String createRegistrationVerification(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, boolean paramBoolean, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, null, paramUUID1, paramUUID2, ExternalIdentifier.ExternalIdType.RegistrationVerification, true, paramBoolean, paramExternalIdData, null)).id;
  }
  
  public void createRememberOAuthScopeConsentChoice(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    createExternalIdentifier(paramTenant, null, paramUUID1, paramUUID2, ExternalIdentifier.ExternalIdType.RememberOAuthScopeConsentChoice, true, false, paramExternalIdData, null);
  }
  
  public String createSAMLAuthNRequestId(Tenant paramTenant, UUID paramUUID, String paramString, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, paramString, null, paramUUID, ExternalIdentifier.ExternalIdType.SAMLv2AuthNRequest, false, false, paramExternalIdData, null)).id;
  }
  
  public void createSAMLAuthNResponseId(Tenant paramTenant, UUID paramUUID, String paramString, ZonedDateTime paramZonedDateTime) {
    createExternalIdentifier(paramTenant, paramString, null, paramUUID, ExternalIdentifier.ExternalIdType.SAMLv2IdPInitiatedAuthNResponse, false, false, null, paramZonedDateTime);
  }
  
  public String createSetupPassword(Tenant paramTenant, UUID paramUUID, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, null, paramUUID, null, ExternalIdentifier.ExternalIdType.SetupPassword, true, true, paramExternalIdData, null)).id;
  }
  
  public String createTrustToken(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, null, paramUUID2, paramUUID1, ExternalIdentifier.ExternalIdType.TrustToken, true, false, paramExternalIdData, null)).id;
  }
  
  public String createTwoFactor(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, null, paramUUID2, paramUUID1, ExternalIdentifier.ExternalIdType.TwoFactor, true, false, paramExternalIdData, null)).id;
  }
  
  public String createTwoFactorOneTimeCode(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, paramString, paramUUID2, paramUUID1, ExternalIdentifier.ExternalIdType.TwoFactorOneTimeCode, true, false, paramExternalIdData, null)).id;
  }
  
  public String createTwoFactorTrust(Tenant paramTenant, UUID paramUUID, ExternalIdentifier.ExternalIdData paramExternalIdData, ZonedDateTime paramZonedDateTime) {
    return (createExternalIdentifier(paramTenant, null, paramUUID, null, ExternalIdentifier.ExternalIdType.TwoFactorTrust, false, false, paramExternalIdData, paramZonedDateTime)).id;
  }
  
  public String createWebAuthnAuthenticationChallenge(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, null, paramUUID1, paramUUID2, ExternalIdentifier.ExternalIdType.WebAuthnAuthenticationChallenge, true, false, paramExternalIdData, null)).id;
  }
  
  public String createWebAuthnRegistrationChallenge(Tenant paramTenant, UUID paramUUID, ExternalIdentifier.ExternalIdData paramExternalIdData) {
    return (createExternalIdentifier(paramTenant, null, paramUUID, null, ExternalIdentifier.ExternalIdType.WebAuthnRegistrationChallenge, true, false, paramExternalIdData, null)).id;
  }
  
  public void deleteAllByUserId(UUID paramUUID) {
    this.externalIdentifierMapper.deleteByUserId(paramUUID);
  }
  
  public int deleteById(String paramString) {
    return this.externalIdentifierMapper.deleteByIds(List.of(paramString));
  }
  
  public void deleteByIds(List<String> paramList) {
    if (!paramList.isEmpty())
      this.externalIdentifierMapper.deleteByIds(paramList); 
  }
  
  public int deleteByUserId(UUID paramUUID, String paramString, ExternalIdentifier.ExternalIdType... paramVarArgs) {
    return this.externalIdentifierMapper.deleteByUserIdAndType(paramUUID, paramString, paramVarArgs);
  }
  
  public int deleteByUserId(UUID paramUUID, ExternalIdentifier.ExternalIdType... paramVarArgs) {
    return this.externalIdentifierMapper.deleteByUserIdAndType(paramUUID, null, paramVarArgs);
  }
  
  public void deleteByUserIdApplicationIdAndType(UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdType paramExternalIdType) {
    this.externalIdentifierMapper.deleteByUserIdApplicationIdAndType(paramUUID1, paramUUID2, paramExternalIdType);
  }
  
  public void deleteIdentityVerificationsByUserAndIdentityType(UUID paramUUID, String paramString, IdentityType paramIdentityType) {
    if (paramUUID == null)
      return; 
    List<String> list = this.externalIdentifierReader.retrieveAllByUserId(paramUUID, IdentityExternalIdHelper.identityExternalIdTypes).stream().filter(paramExternalIdentifier -> IdentityExternalIdHelper.matchesIdentity(paramExternalIdentifier, paramString, paramIdentityType)).map(paramExternalIdentifier -> paramExternalIdentifier.id).toList();
    if (!list.isEmpty())
      this.externalIdentifierMapper.deleteByIds(list); 
  }
  
  public String generateExternalId(Tenant paramTenant, ExternalIdentifier.ExternalIdType paramExternalIdType) {
    String str = null;
    SecureGeneratorConfiguration secureGeneratorConfiguration = getGeneratorConfiguration(paramTenant, paramExternalIdType);
    byte b = 10;
    while (str == null && b > 0) {
      b--;
      switch (secureGeneratorConfiguration.type) {
        case AuthorizationCode:
          str = SecurityTools.secureRandom(secureGeneratorConfiguration.length);
          break;
        case AuthorizedSupportId:
          str = SecurityTools.secureRandomAlpha(secureGeneratorConfiguration.length);
          break;
        case ChangePassword:
          str = SecurityTools.secureRandomAlphaNumeric(secureGeneratorConfiguration.length);
          break;
        default:
          str = SecurityTools.secureRandomNumber(secureGeneratorConfiguration.length);
          break;
      } 
      ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveById(str);
      if (externalIdentifier != null)
        str = null; 
    } 
    if (str == null)
      throw new ServiceUnavailableException(); 
    return str;
  }
  
  public String generateRegistrationVerificationOneTimeCode(Tenant paramTenant) {
    return generateExternalId(paramTenant, ExternalIdentifier.ExternalIdType.RegistrationVerificationOneTimeCode);
  }
  
  public String generateTwoFactorOneTimeCode(Tenant paramTenant) {
    return generateExternalId(paramTenant, ExternalIdentifier.ExternalIdType.TwoFactorOneTimeCode);
  }
  
  public int update(ExternalIdentifier paramExternalIdentifier) {
    return this.externalIdentifierMapper.update(paramExternalIdentifier);
  }
  
  private SecureGeneratorConfiguration getGeneratorConfiguration(Tenant paramTenant, ExternalIdentifier.ExternalIdType paramExternalIdType) {
    switch (paramExternalIdType) {
      default:
        throw new MatchException(null, null);
      case AuthorizationCode:
      
      case AuthorizedSupportId:
      
      case ChangePassword:
      
      case DeviceCode:
      
      case DeviceUserCode:
      
      case EmailVerification:
      
      case EmailVerificationOneTimeCode:
      
      case ExternalAuthentication:
      
      case LoginIntent:
      
      case OneTimePassword:
      
      case PasswordlessLogin:
      
      case PasswordlessLoginOneTimeCode:
      
      case PendingIdPLinkId:
      
      case PhoneVerification:
      
      case PhoneVerificationOneTimeCode:
      
      case RegistrationVerification:
      
      case RegistrationVerificationOneTimeCode:
      
      case RememberOAuthScopeConsentChoice:
      
      case TwoFactor:
      
      case TwoFactorOneTimeCode:
      
      case TwoFactorTrust:
      
      case SAMLv2AuthNRequest:
      
      case SAMLv2IdPInitiatedAuthNResponse:
      
      case SetupPassword:
      
      case TrustToken:
      
      case WebAuthnAuthenticationChallenge:
      
      case WebAuthnRegistrationChallenge:
      
      case IdentityProviderConnectionTest:
        break;
    } 
    return 


























      
      defaultConfig;
  }
}
