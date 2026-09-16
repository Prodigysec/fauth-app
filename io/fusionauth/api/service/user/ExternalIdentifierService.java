package io.fusionauth.api.service.user;

import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.PasswordlessStrategy;
import io.fusionauth.domain.Tenant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface ExternalIdentifierService {
  String createAuthorizationCode(Tenant paramTenant, UUID paramUUID, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createAuthorizedSupportId(Tenant paramTenant, ExternalIdentifier.ExternalIdData paramExternalIdData, ZonedDateTime paramZonedDateTime);
  
  String createChangePassword(Tenant paramTenant, UUID paramUUID, String paramString, boolean paramBoolean, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createDeviceCode(Tenant paramTenant, UUID paramUUID, String paramString, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createDeviceUserCode(Tenant paramTenant, UUID paramUUID, String paramString1, String paramString2, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createExternalAuthentication(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createIdentityProviderConnectionTest(Tenant paramTenant, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  ExternalIdentifier createIdentityVerification(Tenant paramTenant, ExternalIdentifier.ExternalIdType paramExternalIdType, ExternalIdentifier.ExternalIdData paramExternalIdData, UUID paramUUID1, UUID paramUUID2);
  
  String createLoginIntent(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createOneTimePassword(Tenant paramTenant, UUID paramUUID, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  ExternalIdentifier createPasswordlessLogin(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, boolean paramBoolean, PasswordlessStrategy paramPasswordlessStrategy, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createPendingIdPLinkId(Tenant paramTenant, UUID paramUUID, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createRegistrationVerification(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, boolean paramBoolean, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  void createRememberOAuthScopeConsentChoice(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createSAMLAuthNRequestId(Tenant paramTenant, UUID paramUUID, String paramString, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  void createSAMLAuthNResponseId(Tenant paramTenant, UUID paramUUID, String paramString, ZonedDateTime paramZonedDateTime);
  
  String createSetupPassword(Tenant paramTenant, UUID paramUUID, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createTrustToken(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createTwoFactor(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createTwoFactorOneTimeCode(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createTwoFactorTrust(Tenant paramTenant, UUID paramUUID, ExternalIdentifier.ExternalIdData paramExternalIdData, ZonedDateTime paramZonedDateTime);
  
  String createWebAuthnAuthenticationChallenge(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  String createWebAuthnRegistrationChallenge(Tenant paramTenant, UUID paramUUID, ExternalIdentifier.ExternalIdData paramExternalIdData);
  
  void deleteAllByUserId(UUID paramUUID);
  
  int deleteById(String paramString);
  
  void deleteByIds(List<String> paramList);
  
  int deleteByUserId(UUID paramUUID, ExternalIdentifier.ExternalIdType... paramVarArgs);
  
  int deleteByUserId(UUID paramUUID, String paramString, ExternalIdentifier.ExternalIdType... paramVarArgs);
  
  void deleteByUserIdApplicationIdAndType(UUID paramUUID1, UUID paramUUID2, ExternalIdentifier.ExternalIdType paramExternalIdType);
  
  void deleteIdentityVerificationsByUserAndIdentityType(UUID paramUUID, String paramString, IdentityType paramIdentityType);
  
  String generateExternalId(Tenant paramTenant, ExternalIdentifier.ExternalIdType paramExternalIdType);
  
  String generateRegistrationVerificationOneTimeCode(Tenant paramTenant);
  
  String generateTwoFactorOneTimeCode(Tenant paramTenant);
  
  int update(ExternalIdentifier paramExternalIdentifier);
}
