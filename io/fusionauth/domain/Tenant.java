package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorPolicy;
import io.fusionauth.domain.util.Normalizer;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Tenant implements Buildable<Tenant>, JSONColumnable {
  public final Map<String, Object> data = new LinkedHashMap<>();
  
  @JSONColumn
  public TenantAccessControlConfiguration accessControlConfiguration = new TenantAccessControlConfiguration();
  
  @JSONColumn
  public URI baseURL;
  
  @JSONColumn
  public TenantCaptchaConfiguration captchaConfiguration = new TenantCaptchaConfiguration();
  
  @JSONColumn
  public ClientRiskConfiguration clientRiskConfiguration = new ClientRiskConfiguration();
  
  @JSONColumn
  public boolean configured;
  
  public List<ConnectorPolicy> connectorPolicies = new ArrayList<>();
  
  @JSONColumn
  public EmailConfiguration emailConfiguration = new EmailConfiguration();
  
  @JSONColumn
  public EventConfiguration eventConfiguration = new EventConfiguration();
  
  @JSONColumn
  public ExternalIdentifierConfiguration externalIdentifierConfiguration = new ExternalIdentifierConfiguration();
  
  @JSONColumn
  public FailedAuthenticationConfiguration failedAuthenticationConfiguration = new FailedAuthenticationConfiguration();
  
  @JSONColumn
  public FamilyConfiguration familyConfiguration = new FamilyConfiguration();
  
  @JSONColumn
  public TenantFormConfiguration formConfiguration = new TenantFormConfiguration();
  
  @JSONColumn
  public int httpSessionMaxInactiveInterval = 3600;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  @JSONColumn
  public String issuer;
  
  @JSONColumn
  @JsonIgnoreProperties({"enabled"})
  public JWTConfiguration jwtConfiguration = new JWTConfiguration();
  
  public TenantLambdaConfiguration lambdaConfiguration = new TenantLambdaConfiguration();
  
  public ZonedDateTime lastUpdateInstant;
  
  @JSONColumn
  public TenantLoginConfiguration loginConfiguration = new TenantLoginConfiguration();
  
  @JSONColumn
  public URI logoutURL;
  
  @JSONColumn
  public MaximumPasswordAge maximumPasswordAge = new MaximumPasswordAge();
  
  @JSONColumn
  public MinimumPasswordAge minimumPasswordAge = new MinimumPasswordAge();
  
  @JSONColumn
  public TenantMultiFactorConfiguration multiFactorConfiguration = new TenantMultiFactorConfiguration();
  
  public String name;
  
  public TenantOAuth2Configuration oauthConfiguration = new TenantOAuth2Configuration();
  
  @JSONColumn
  public PasswordEncryptionConfiguration passwordEncryptionConfiguration = new PasswordEncryptionConfiguration();
  
  @JSONColumn
  public PasswordValidationRules passwordValidationRules = new PasswordValidationRules();
  
  @JSONColumn
  public TenantPhoneConfiguration phoneConfiguration = new TenantPhoneConfiguration();
  
  @JSONColumn
  public TenantRateLimitConfiguration rateLimitConfiguration = new TenantRateLimitConfiguration();
  
  @JSONColumn
  public TenantRegistrationConfiguration registrationConfiguration = new TenantRegistrationConfiguration();
  
  @JSONColumn
  public TenantSCIMServerConfiguration scimServerConfiguration = new TenantSCIMServerConfiguration();
  
  @JSONColumn
  public TenantSSOConfiguration ssoConfiguration = new TenantSSOConfiguration();
  
  @JSONColumn
  public ObjectState state;
  
  public UUID themeId;
  
  @JSONColumn
  public TenantUserDeletePolicy userDeletePolicy = new TenantUserDeletePolicy();
  
  @JSONColumn
  public TenantUsernameConfiguration usernameConfiguration = new TenantUsernameConfiguration();
  
  @JSONColumn
  public TenantWebAuthnConfiguration webAuthnConfiguration = new TenantWebAuthnConfiguration();
  
  public Tenant(Tenant paramTenant) {
    this.baseURL = paramTenant.baseURL;
    this.captchaConfiguration = new TenantCaptchaConfiguration(paramTenant.captchaConfiguration);
    this.clientRiskConfiguration = new ClientRiskConfiguration(paramTenant.clientRiskConfiguration);
    this.configured = paramTenant.configured;
    this.connectorPolicies.addAll((Collection<? extends ConnectorPolicy>)paramTenant.connectorPolicies.stream().map(ConnectorPolicy::new).collect(Collectors.toList()));
    this.data.putAll(paramTenant.data);
    this.emailConfiguration = new EmailConfiguration(paramTenant.emailConfiguration);
    this.eventConfiguration = new EventConfiguration(paramTenant.eventConfiguration);
    this.externalIdentifierConfiguration = new ExternalIdentifierConfiguration(paramTenant.externalIdentifierConfiguration);
    this.failedAuthenticationConfiguration = new FailedAuthenticationConfiguration(paramTenant.failedAuthenticationConfiguration);
    this.familyConfiguration = new FamilyConfiguration(paramTenant.familyConfiguration);
    this.formConfiguration = new TenantFormConfiguration(paramTenant.formConfiguration);
    this.httpSessionMaxInactiveInterval = paramTenant.httpSessionMaxInactiveInterval;
    this.id = paramTenant.id;
    this.insertInstant = paramTenant.insertInstant;
    this.accessControlConfiguration = new TenantAccessControlConfiguration(paramTenant.accessControlConfiguration);
    this.issuer = paramTenant.issuer;
    this.jwtConfiguration = new JWTConfiguration(paramTenant.jwtConfiguration);
    this.lambdaConfiguration = new TenantLambdaConfiguration(paramTenant.lambdaConfiguration);
    this.lastUpdateInstant = paramTenant.lastUpdateInstant;
    this.loginConfiguration = new TenantLoginConfiguration(paramTenant.loginConfiguration);
    this.logoutURL = paramTenant.logoutURL;
    this.maximumPasswordAge = new MaximumPasswordAge(paramTenant.maximumPasswordAge);
    this.minimumPasswordAge = new MinimumPasswordAge(paramTenant.minimumPasswordAge);
    this.multiFactorConfiguration = new TenantMultiFactorConfiguration(paramTenant.multiFactorConfiguration);
    this.name = paramTenant.name;
    this.oauthConfiguration = new TenantOAuth2Configuration(paramTenant.oauthConfiguration);
    this.passwordEncryptionConfiguration = new PasswordEncryptionConfiguration(paramTenant.passwordEncryptionConfiguration);
    this.passwordValidationRules = new PasswordValidationRules(paramTenant.passwordValidationRules);
    this.phoneConfiguration = new TenantPhoneConfiguration(paramTenant.phoneConfiguration);
    this.rateLimitConfiguration = new TenantRateLimitConfiguration(paramTenant.rateLimitConfiguration);
    this.registrationConfiguration = new TenantRegistrationConfiguration(paramTenant.registrationConfiguration);
    this.scimServerConfiguration = new TenantSCIMServerConfiguration(paramTenant.scimServerConfiguration);
    this.ssoConfiguration = new TenantSSOConfiguration(paramTenant.ssoConfiguration);
    this.state = paramTenant.state;
    this.themeId = paramTenant.themeId;
    this.userDeletePolicy = new TenantUserDeletePolicy(paramTenant.userDeletePolicy);
    this.usernameConfiguration = new TenantUsernameConfiguration(paramTenant.usernameConfiguration);
    this.webAuthnConfiguration = new TenantWebAuthnConfiguration(paramTenant.webAuthnConfiguration);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Tenant))
      return false; 
    Tenant tenant = (Tenant)paramObject;
    return (this.configured == tenant.configured && this.httpSessionMaxInactiveInterval == tenant.httpSessionMaxInactiveInterval && 
      
      Objects.equals(this.baseURL, tenant.baseURL) && 
      Objects.equals(this.captchaConfiguration, tenant.captchaConfiguration) && 
      Objects.equals(this.clientRiskConfiguration, tenant.clientRiskConfiguration) && 
      Objects.equals(this.connectorPolicies, tenant.connectorPolicies) && 
      Objects.equals(this.data, tenant.data) && 
      Objects.equals(this.emailConfiguration, tenant.emailConfiguration) && 
      Objects.equals(this.eventConfiguration, tenant.eventConfiguration) && 
      Objects.equals(this.externalIdentifierConfiguration, tenant.externalIdentifierConfiguration) && 
      Objects.equals(this.failedAuthenticationConfiguration, tenant.failedAuthenticationConfiguration) && 
      Objects.equals(this.familyConfiguration, tenant.familyConfiguration) && 
      Objects.equals(this.formConfiguration, tenant.formConfiguration) && 
      Objects.equals(this.id, tenant.id) && 
      Objects.equals(this.insertInstant, tenant.insertInstant) && 
      Objects.equals(this.accessControlConfiguration, tenant.accessControlConfiguration) && 
      Objects.equals(this.issuer, tenant.issuer) && 
      Objects.equals(this.jwtConfiguration, tenant.jwtConfiguration) && 
      Objects.equals(this.lambdaConfiguration, tenant.lambdaConfiguration) && 
      Objects.equals(this.lastUpdateInstant, tenant.lastUpdateInstant) && 
      Objects.equals(this.loginConfiguration, tenant.loginConfiguration) && 
      Objects.equals(this.logoutURL, tenant.logoutURL) && 
      Objects.equals(this.maximumPasswordAge, tenant.maximumPasswordAge) && 
      Objects.equals(this.minimumPasswordAge, tenant.minimumPasswordAge) && 
      Objects.equals(this.multiFactorConfiguration, tenant.multiFactorConfiguration) && 
      Objects.equals(this.name, tenant.name) && 
      Objects.equals(this.passwordEncryptionConfiguration, tenant.passwordEncryptionConfiguration) && 
      Objects.equals(this.passwordValidationRules, tenant.passwordValidationRules) && 
      Objects.equals(this.phoneConfiguration, tenant.phoneConfiguration) && 
      Objects.equals(this.rateLimitConfiguration, tenant.rateLimitConfiguration) && 
      Objects.equals(this.registrationConfiguration, tenant.registrationConfiguration) && 
      Objects.equals(this.scimServerConfiguration, tenant.scimServerConfiguration) && 
      Objects.equals(this.ssoConfiguration, tenant.ssoConfiguration) && 
      Objects.equals(this.state, tenant.state) && 
      Objects.equals(this.themeId, tenant.themeId) && 
      Objects.equals(this.userDeletePolicy, tenant.userDeletePolicy) && 
      Objects.equals(this.usernameConfiguration, tenant.usernameConfiguration) && 
      Objects.equals(this.webAuthnConfiguration, tenant.webAuthnConfiguration));
  }
  
  @JsonIgnore
  public ConnectorPolicy getPolicyByConnectorId(UUID paramUUID) {
    return this.connectorPolicies.stream().filter(paramConnectorPolicy -> paramConnectorPolicy.connectorId.equals(paramUUID)).findFirst().orElse(null);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.baseURL, this.captchaConfiguration, this.clientRiskConfiguration, 

          
          Boolean.valueOf(this.configured), this.connectorPolicies, this.data, this.emailConfiguration, this.eventConfiguration, this.externalIdentifierConfiguration, this.failedAuthenticationConfiguration, 
          this.familyConfiguration, this.formConfiguration, 







          
          Integer.valueOf(this.httpSessionMaxInactiveInterval), this.id, this.insertInstant, this.accessControlConfiguration, this.issuer, this.jwtConfiguration, this.lambdaConfiguration, this.lastUpdateInstant, 
          this.loginConfiguration, this.logoutURL, this.maximumPasswordAge, this.minimumPasswordAge, this.name, this.passwordEncryptionConfiguration, this.passwordValidationRules, this.phoneConfiguration, this.rateLimitConfiguration, this.registrationConfiguration, 
          this.scimServerConfiguration, this.state, this.themeId, this.userDeletePolicy, this.usernameConfiguration, this.webAuthnConfiguration });
  }
  
  @JsonIgnore
  public JWTConfiguration lookupJWTConfiguration(Application paramApplication) {
    if (paramApplication != null && paramApplication.jwtConfiguration != null && paramApplication.jwtConfiguration.enabled)
      return paramApplication.jwtConfiguration; 
    return this.jwtConfiguration;
  }
  
  @JsonIgnore
  public MultiFactorLoginPolicy lookupMultiFactorLoginPolicy(Application paramApplication) {
    if (paramApplication != null && paramApplication.multiFactorConfiguration.loginPolicy != null)
      return paramApplication.multiFactorConfiguration.loginPolicy; 
    return this.multiFactorConfiguration.loginPolicy;
  }
  
  public void normalize() {
    if (!this.emailConfiguration.verifyEmail) {
      this.emailConfiguration.verifyEmailWhenChanged = false;
      this.emailConfiguration.verificationEmailTemplateId = null;
    } 
    Normalizer.removeEmpty(this.data);
    this.name = Normalizer.trim(this.name);
    this.emailConfiguration.normalize();
    this.eventConfiguration.normalize();
    this.connectorPolicies.forEach(paramConnectorPolicy -> Normalizer.toLowerCase(paramConnectorPolicy.domains, java.util.HashSet::new));
    if (this.connectorPolicies.isEmpty()) {
      this.connectorPolicies.add((new ConnectorPolicy()).with(paramConnectorPolicy -> paramConnectorPolicy.connectorId = BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID)
          .with(paramConnectorPolicy -> paramConnectorPolicy.domains.add("*")));
    } else {
      this.connectorPolicies.stream()
        .filter(paramConnectorPolicy -> paramConnectorPolicy.connectorId.equals(BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID))
        .forEach(paramConnectorPolicy -> paramConnectorPolicy.with(()).with(()).with(()));
    } 
    this.familyConfiguration.normalize();
  }
  
  public Tenant secure() {
    this.emailConfiguration.password = null;
    return this;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  @JacksonConstructor
  public Tenant() {}
  
  public static class TenantOAuth2Configuration {
    public UUID clientCredentialsAccessTokenPopulateLambdaId;
    
    public TenantOAuth2Configuration() {}
    
    public TenantOAuth2Configuration(TenantOAuth2Configuration param1TenantOAuth2Configuration) {
      this.clientCredentialsAccessTokenPopulateLambdaId = param1TenantOAuth2Configuration.clientCredentialsAccessTokenPopulateLambdaId;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (!(param1Object instanceof TenantOAuth2Configuration))
        return false; 
      TenantOAuth2Configuration tenantOAuth2Configuration = (TenantOAuth2Configuration)param1Object;
      return Objects.equals(this.clientCredentialsAccessTokenPopulateLambdaId, tenantOAuth2Configuration.clientCredentialsAccessTokenPopulateLambdaId);
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.clientCredentialsAccessTokenPopulateLambdaId });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
}
