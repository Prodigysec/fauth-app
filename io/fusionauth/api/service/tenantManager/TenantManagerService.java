package io.fusionauth.api.service.tenantManager;

import com.inversoft.error.Errors;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.provider.IdentityProviderLinkingStrategy;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.tenantManager.TenantManagerConfiguration;
import io.fusionauth.domain.tenantManager.TenantManagerIdentityProviderTypeConfiguration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public interface TenantManagerService {
  public static final Set<String> RestrictedAttributeMappingKeys = Set.of("user.password");
  
  public static final Set<IdentityProviderType> SupportedTenantManagerIdentityProviderTypes = new LinkedHashSet<>(Arrays.asList(new IdentityProviderType[] { IdentityProviderType.OpenIDConnect, IdentityProviderType.SAMLv2 }));
  
  public static final Set<IdentityProviderLinkingStrategy> ValidTenantManagerIdentityProviderLinkingStrategies = new LinkedHashSet<>(Arrays.asList(new IdentityProviderLinkingStrategy[] { IdentityProviderLinkingStrategy.LinkByEmail, IdentityProviderLinkingStrategy.LinkByEmailForExistingUser, IdentityProviderLinkingStrategy.LinkByUsername, IdentityProviderLinkingStrategy.LinkByUsernameForExistingUser }));
  
  void createTenantManagerIdentityProviderTypeConfiguration(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration);
  
  void deleteTenantManagerIdentityProviderTypeConfiguration(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration);
  
  TenantManagerConfiguration retrieve();
  
  TenantManagerIdentityProviderTypeConfiguration retrieveTenantManagerIdentityProviderTypeConfiguration(IdentityProviderType paramIdentityProviderType);
  
  void update(TenantManagerConfiguration paramTenantManagerConfiguration);
  
  void updateTenantManagerIdentityProviderTypeConfiguration(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration1, TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration2);
  
  Errors validate(TenantManagerConfiguration paramTenantManagerConfiguration);
  
  TenantManagerIdentityProviderTypeConfigurationValidationResult validateTenantManagerIdentityProviderTypeConfigurationCreate(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration);
  
  TenantManagerIdentityProviderTypeConfigurationValidationResult validateTenantManagerIdentityProviderTypeConfigurationDelete(IdentityProviderType paramIdentityProviderType);
  
  TenantManagerIdentityProviderTypeConfigurationValidationResult validateTenantManagerIdentityProviderTypeConfigurationUpdate(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration);
  
  public static class TenantManagerIdentityProviderTypeConfigurationValidationResult extends BaseValidationResult {
    public TenantManagerIdentityProviderTypeConfiguration existing;
    
    public TenantManagerIdentityProviderTypeConfiguration typeConfiguration;
    
    public TenantManagerIdentityProviderTypeConfigurationValidationResult() {}
    
    public TenantManagerIdentityProviderTypeConfigurationValidationResult(TenantManagerIdentityProviderTypeConfiguration param1TenantManagerIdentityProviderTypeConfiguration) {
      this.typeConfiguration = param1TenantManagerIdentityProviderTypeConfiguration;
    }
  }
}
