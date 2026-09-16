package io.fusionauth.api.service.reactor;

import com.google.inject.Inject;
import com.inversoft.license.v2.LicenseProvider;
import com.inversoft.license.v2.domain.ExpirationLicenseScheme;
import com.inversoft.license.v2.domain.License;
import com.inversoft.license.v2.domain.LicenseContainer;
import com.inversoft.license.v2.domain.LicenseFeature;
import com.inversoft.license.v2.domain.LicenseFeatureCapabilities;
import com.inversoft.license.v2.domain.LicenseFeatureType;
import com.inversoft.license.v2.domain.LicenseScheme;
import com.inversoft.license.v2.domain.LicenseSchemeType;
import com.inversoft.license.v2.domain.fusionauth.FusionAuthCoreFeature;
import io.fusionauth.api.domain.ReactorHealthChecks;
import io.fusionauth.api.service.cache.InstanceCache;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.function.Function;

public class DefaultReactorStatusService implements ReactorStatusService {
  private final InstanceCache instanceCache;
  
  private final LicenseProvider licenseProvider;
  
  @Inject
  public DefaultReactorStatusService(InstanceCache paramInstanceCache, LicenseProvider paramLicenseProvider) {
    this.instanceCache = paramInstanceCache;
    this.licenseProvider = paramLicenseProvider;
  }
  
  public static boolean isLicensedForIPGeoLocation(License paramLicense) {
    return (paramLicense != null && paramLicense.hasFeature(LicenseFeatureType.FusionAuthThreatDetection));
  }
  
  public static boolean isLicensedForIPReputation(License paramLicense) {
    return (paramLicense != null && paramLicense.hasFeature(LicenseFeatureType.FusionAuthIntelligentMFA));
  }
  
  public static boolean isLicensedForUserAgentReputation(License paramLicense) {
    return (paramLicense != null && paramLicense.hasFeature(LicenseFeatureType.FusionAuthIntelligentMFA));
  }
  
  public ReactorStatus retrieveStatus() {
    ReactorStatus reactorStatus = buildReactorStatusFromLicense();
    ReactorHealthChecks reactorHealthChecks = (this.instanceCache.get()).reactorHealthChecks;
    reactorHealthChecks.updateWithHealth(reactorStatus);
    return reactorStatus;
  }
  
  private ReactorStatus buildReactorStatusFromLicense() {
    LicenseContainer licenseContainer = this.licenseProvider.getLicense();
    ReactorStatus reactorStatus = new ReactorStatus();
    if (ReactorService.isLicenseContainerInvalid(licenseContainer))
      return reactorStatus; 
    reactorStatus.licensed = true;
    License license = licenseContainer.license();
    Function<LicenseFeatureType, ReactorFeatureStatus> function = paramLicenseFeatureType -> paramLicense.hasFeature(paramLicenseFeatureType) ? ReactorFeatureStatus.ACTIVE : ReactorFeatureStatus.DISABLED;
    reactorStatus.advancedIdentityProviders = function.apply(LicenseFeatureType.FusionAuthAdvancedIdentityProviders);
    reactorStatus.advancedLambdas = function.apply(LicenseFeatureType.FusionAuthAdvancedLambdas);
    LicenseFeature licenseFeature1 = (LicenseFeature)license.features.get(LicenseFeatureType.FusionAuthAdvancedMultiFactor);
    if (licenseFeature1 instanceof LicenseFeatureCapabilities) {
      LicenseFeatureCapabilities licenseFeatureCapabilities = (LicenseFeatureCapabilities)licenseFeature1;
      reactorStatus.advancedMultiFactorAuthentication = ReactorFeatureStatus.ACTIVE;
      reactorStatus.applicationMultiFactorAuthentication = licenseFeatureCapabilities.hasCapability("ApplicationConfiguration") ? ReactorFeatureStatus.ACTIVE : ReactorFeatureStatus.DISABLED;
      reactorStatus.multiFactorLambdas = licenseFeatureCapabilities.hasCapability("Lambdas") ? ReactorFeatureStatus.ACTIVE : ReactorFeatureStatus.DISABLED;
    } else {
      reactorStatus.advancedMultiFactorAuthentication = function.apply(LicenseFeatureType.FusionAuthAdvancedMultiFactor);
      reactorStatus.applicationMultiFactorAuthentication = ReactorFeatureStatus.DISABLED;
      reactorStatus.multiFactorLambdas = ReactorFeatureStatus.DISABLED;
    } 
    LicenseFeature licenseFeature2 = (LicenseFeature)license.features.get(LicenseFeatureType.FusionAuthIntelligentMFA);
    if (licenseFeature2 instanceof LicenseFeatureCapabilities) {
      LicenseFeatureCapabilities licenseFeatureCapabilities = (LicenseFeatureCapabilities)licenseFeature2;
      reactorStatus.intelligentMFA = ReactorFeatureStatus.ACTIVE;
      reactorStatus.clientRiskConfiguration = licenseFeatureCapabilities.hasCapability("SignalConfiguration") ? ReactorFeatureStatus.ACTIVE : ReactorFeatureStatus.DISABLED;
      reactorStatus.imfaWebhooks = licenseFeatureCapabilities.hasCapability("WebhookEvents") ? ReactorFeatureStatus.ACTIVE : ReactorFeatureStatus.DISABLED;
    } else {
      reactorStatus.intelligentMFA = function.apply(LicenseFeatureType.FusionAuthIntelligentMFA);
      reactorStatus.clientRiskConfiguration = ReactorFeatureStatus.DISABLED;
      reactorStatus.imfaWebhooks = ReactorFeatureStatus.DISABLED;
    } 
    reactorStatus.advancedRegistration = function.apply(LicenseFeatureType.FusionAuthAdvancedRegistration);
    reactorStatus.applicationThemes = function.apply(LicenseFeatureType.FusionAuthApplicationThemes);
    reactorStatus.connectors = function.apply(LicenseFeatureType.FusionAuthConnectors);
    reactorStatus.dPoP = function.apply(LicenseFeatureType.FusionAuthDPoP);
    reactorStatus.entityManagement = function.apply(LicenseFeatureType.FusionAuthEntityManagement);
    reactorStatus.legacyAdapter = function.apply(LicenseFeatureType.FusionAuthLegacyAdapter);
    reactorStatus.scimServer = function.apply(LicenseFeatureType.FusionAuthSCIMServer);
    reactorStatus.threatDetection = function.apply(LicenseFeatureType.FusionAuthThreatDetection);
    reactorStatus.tenantManagerApplication = function.apply(LicenseFeatureType.FusionAuthOrganizationAdminApplication);
    reactorStatus.universalApplication = function.apply(LicenseFeatureType.FusionAuthUniversalApplication);
    LicenseFeature licenseFeature3 = (LicenseFeature)license.features.get(LicenseFeatureType.FusionAuthWebAuthn);
    if (licenseFeature3 instanceof LicenseFeatureCapabilities) {
      LicenseFeatureCapabilities licenseFeatureCapabilities = (LicenseFeatureCapabilities)licenseFeature3;
      reactorStatus.webAuthn = ReactorFeatureStatus.ACTIVE;
      reactorStatus.webAuthnPlatformAuthenticators = licenseFeatureCapabilities.hasCapability("PlatformAuthenticators") ? ReactorFeatureStatus.ACTIVE : ReactorFeatureStatus.DISABLED;
      reactorStatus.webAuthnRoamingAuthenticators = licenseFeatureCapabilities.hasCapability("RoamingAuthenticators") ? ReactorFeatureStatus.ACTIVE : ReactorFeatureStatus.DISABLED;
    } else {
      reactorStatus.webAuthn = ReactorFeatureStatus.DISABLED;
      reactorStatus.webAuthnPlatformAuthenticators = ReactorFeatureStatus.DISABLED;
      reactorStatus.webAuthnRoamingAuthenticators = ReactorFeatureStatus.DISABLED;
    } 
    LicenseFeature licenseFeature4 = (LicenseFeature)license.features.get(LicenseFeatureType.FusionAuthAdvancedOAuthScopes);
    if (licenseFeature4 instanceof LicenseFeatureCapabilities) {
      LicenseFeatureCapabilities licenseFeatureCapabilities = (LicenseFeatureCapabilities)licenseFeature4;
      reactorStatus.advancedOAuthScopes = ReactorFeatureStatus.ACTIVE;
      reactorStatus.advancedOAuthScopesCustomScopes = licenseFeatureCapabilities.hasCapability("CustomScopes") ? ReactorFeatureStatus.ACTIVE : ReactorFeatureStatus.DISABLED;
      reactorStatus.advancedOAuthScopesThirdPartyApplications = licenseFeatureCapabilities.hasCapability("ThirdPartyApplications") ? ReactorFeatureStatus.ACTIVE : ReactorFeatureStatus.DISABLED;
    } else {
      reactorStatus.advancedOAuthScopes = ReactorFeatureStatus.DISABLED;
      reactorStatus.advancedOAuthScopesCustomScopes = ReactorFeatureStatus.DISABLED;
      reactorStatus.advancedOAuthScopesThirdPartyApplications = ReactorFeatureStatus.DISABLED;
    } 
    if (license.airGapped) {
      LicenseScheme licenseScheme = (LicenseScheme)license.schemes.get(LicenseSchemeType.Expiration);
      if (licenseScheme != null)
        reactorStatus.expiration = ((ExpirationLicenseScheme)licenseScheme).expiration(); 
    } 
    FusionAuthCoreFeature fusionAuthCoreFeature = (FusionAuthCoreFeature)license.features.get(LicenseFeatureType.FusionAuthCore);
    reactorStatus.licenseAttributes.putAll(fusionAuthCoreFeature.getAttributes());
    return reactorStatus;
  }
}
