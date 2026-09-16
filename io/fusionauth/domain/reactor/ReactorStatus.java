package io.fusionauth.domain.reactor;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReactorStatus {
  public ReactorFeatureStatus advancedIdentityProviders = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus advancedLambdas = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus advancedMultiFactorAuthentication = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus advancedOAuthScopes = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus advancedOAuthScopesCustomScopes = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus advancedOAuthScopesThirdPartyApplications = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus advancedRegistration = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus applicationMultiFactorAuthentication = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus applicationThemes = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus breachedPasswordDetection = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus clientRiskConfiguration = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus connectors = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus dPoP = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus entityManagement = ReactorFeatureStatus.UNKNOWN;
  
  public LocalDate expiration;
  
  public ReactorFeatureStatus imfaWebhooks = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus intelligentMFA = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus ipGeoLocation = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus ipReputation = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus legacyAdapter = ReactorFeatureStatus.UNKNOWN;
  
  public Map<String, String> licenseAttributes = new HashMap<>();
  
  public boolean licensed;
  
  public ReactorFeatureStatus multiFactorLambdas = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus scimServer = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus tenantManagerApplication = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus threatDetection = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus universalApplication = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus userAgentReputation = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus webAuthn = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus webAuthnPlatformAuthenticators = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus webAuthnRoamingAuthenticators = ReactorFeatureStatus.UNKNOWN;
  
  @JacksonConstructor
  public ReactorStatus() {}
  
  public ReactorStatus(ReactorStatus paramReactorStatus) {
    this.advancedIdentityProviders = paramReactorStatus.advancedIdentityProviders;
    this.advancedLambdas = paramReactorStatus.advancedLambdas;
    this.advancedMultiFactorAuthentication = paramReactorStatus.advancedMultiFactorAuthentication;
    this.multiFactorLambdas = paramReactorStatus.multiFactorLambdas;
    this.advancedRegistration = paramReactorStatus.advancedRegistration;
    this.applicationMultiFactorAuthentication = paramReactorStatus.applicationMultiFactorAuthentication;
    this.applicationThemes = paramReactorStatus.applicationThemes;
    this.breachedPasswordDetection = paramReactorStatus.breachedPasswordDetection;
    this.connectors = paramReactorStatus.connectors;
    this.advancedOAuthScopes = paramReactorStatus.advancedOAuthScopes;
    this.advancedOAuthScopesCustomScopes = paramReactorStatus.advancedOAuthScopesCustomScopes;
    this.advancedOAuthScopesThirdPartyApplications = paramReactorStatus.advancedOAuthScopesThirdPartyApplications;
    this.clientRiskConfiguration = paramReactorStatus.clientRiskConfiguration;
    this.dPoP = paramReactorStatus.dPoP;
    this.entityManagement = paramReactorStatus.entityManagement;
    this.expiration = paramReactorStatus.expiration;
    this.imfaWebhooks = paramReactorStatus.imfaWebhooks;
    this.intelligentMFA = paramReactorStatus.intelligentMFA;
    this.ipGeoLocation = paramReactorStatus.ipGeoLocation;
    this.ipReputation = paramReactorStatus.ipReputation;
    this.legacyAdapter = paramReactorStatus.legacyAdapter;
    this.licenseAttributes.putAll(paramReactorStatus.licenseAttributes);
    this.licensed = paramReactorStatus.licensed;
    this.tenantManagerApplication = paramReactorStatus.tenantManagerApplication;
    this.scimServer = paramReactorStatus.scimServer;
    this.threatDetection = paramReactorStatus.threatDetection;
    this.universalApplication = paramReactorStatus.universalApplication;
    this.userAgentReputation = paramReactorStatus.userAgentReputation;
    this.webAuthn = paramReactorStatus.webAuthn;
    this.webAuthnPlatformAuthenticators = paramReactorStatus.webAuthnPlatformAuthenticators;
    this.webAuthnRoamingAuthenticators = paramReactorStatus.webAuthnRoamingAuthenticators;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    ReactorStatus reactorStatus = (ReactorStatus)paramObject;
    return (this.advancedIdentityProviders == reactorStatus.advancedIdentityProviders && this.advancedLambdas == reactorStatus.advancedLambdas && this.advancedMultiFactorAuthentication == reactorStatus.advancedMultiFactorAuthentication && this.advancedOAuthScopes == reactorStatus.advancedOAuthScopes && this.advancedOAuthScopesCustomScopes == reactorStatus.advancedOAuthScopesCustomScopes && this.advancedOAuthScopesThirdPartyApplications == reactorStatus.advancedOAuthScopesThirdPartyApplications && this.advancedRegistration == reactorStatus.advancedRegistration && this.applicationMultiFactorAuthentication == reactorStatus.applicationMultiFactorAuthentication && this.applicationThemes == reactorStatus.applicationThemes && this.breachedPasswordDetection == reactorStatus.breachedPasswordDetection && this.connectors == reactorStatus.connectors && this.clientRiskConfiguration == reactorStatus.clientRiskConfiguration && this.dPoP == reactorStatus.dPoP && this.entityManagement == reactorStatus.entityManagement && this.imfaWebhooks == reactorStatus.imfaWebhooks && this.intelligentMFA == reactorStatus.intelligentMFA && this.ipGeoLocation == reactorStatus.ipGeoLocation && this.ipReputation == reactorStatus.ipReputation && this.legacyAdapter == reactorStatus.legacyAdapter && 

















      
      Objects.equals(this.expiration, reactorStatus.expiration) && this.licensed == reactorStatus.licensed && 
      
      Objects.equals(this.licenseAttributes, reactorStatus.licenseAttributes) && this.multiFactorLambdas == reactorStatus.multiFactorLambdas && this.tenantManagerApplication == reactorStatus.tenantManagerApplication && this.scimServer == reactorStatus.scimServer && this.threatDetection == reactorStatus.threatDetection && this.universalApplication == reactorStatus.universalApplication && this.userAgentReputation == reactorStatus.userAgentReputation && this.webAuthn == reactorStatus.webAuthn && this.webAuthnPlatformAuthenticators == reactorStatus.webAuthnPlatformAuthenticators && this.webAuthnRoamingAuthenticators == reactorStatus.webAuthnRoamingAuthenticators);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.advancedIdentityProviders, this.advancedLambdas, this.advancedMultiFactorAuthentication, this.advancedOAuthScopes, this.advancedOAuthScopesCustomScopes, this.advancedOAuthScopesThirdPartyApplications, this.advancedRegistration, this.applicationMultiFactorAuthentication, this.applicationThemes, this.breachedPasswordDetection, 
          this.clientRiskConfiguration, this.connectors, this.dPoP, this.entityManagement, this.expiration, this.imfaWebhooks, this.intelligentMFA, this.ipGeoLocation, this.ipReputation, this.legacyAdapter, 
          Boolean.valueOf(this.licensed), this.licenseAttributes, this.multiFactorLambdas, this.tenantManagerApplication, this.scimServer, this.threatDetection, this.universalApplication, this.userAgentReputation, this.webAuthn, this.webAuthnPlatformAuthenticators, 
          this.webAuthnRoamingAuthenticators });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
