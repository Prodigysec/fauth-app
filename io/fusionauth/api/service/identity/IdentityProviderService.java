package io.fusionauth.api.service.identity;

import com.inversoft.error.Errors;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.identityProvider.IdentityProviderConnectionTestRequest;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderConnectionTestResult;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public interface IdentityProviderService {
  public static final Set<IdentityProviderType> RestrictedIdentityProviderTypes;
  
  void create(BaseIdentityProvider<?> paramBaseIdentityProvider);
  
  boolean delete(BaseIdentityProvider<?> paramBaseIdentityProvider);
  
  void deleteAllByTenantId(UUID paramUUID);
  
  IdentityProviderConnectionTestResult retrieveConnectionTestResult(ExternalIdentifier paramExternalIdentifier);
  
  String startConnectionTest(Tenant paramTenant, IdentityProviderConnectionTestRequest paramIdentityProviderConnectionTestRequest);
  
  int update(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, @Nullable ZonedDateTime paramZonedDateTime);
  
  ValidationResult validate(@Nullable UUID paramUUID, BaseIdentityProvider<?> paramBaseIdentityProvider, boolean paramBoolean);
  
  Errors validateClaim(UUID paramUUID, String paramString1, String paramString2);
  
  ValidationResult validateDelete(@Nullable UUID paramUUID1, UUID paramUUID2);
  
  ValidationResult validateRetrieveConnectionTestResult(@Nullable Tenant paramTenant, String paramString);
  
  ValidationResult validateStartConnectionTest(@Nullable Tenant paramTenant, IdentityProviderConnectionTestRequest paramIdentityProviderConnectionTestRequest);
  
  static {
    RestrictedIdentityProviderTypes = (Set<IdentityProviderType>)Arrays.<IdentityProviderType>stream(IdentityProviderType.values()).filter(paramIdentityProviderType -> (paramIdentityProviderType.id != null)).collect(Collectors.toSet());
  }
  
  public static class ValidationResult extends BaseValidationResult {
    public ExternalIdentifier connectionTestId;
    
    public BaseIdentityProvider<?> existing;
    
    public BaseIdentityProvider<?> identityProvider;
    
    public Tenant tenant;
    
    public ValidationResult() {}
    
    public ValidationResult(BaseIdentityProvider<?> param1BaseIdentityProvider) {
      this.identityProvider = param1BaseIdentityProvider;
    }
  }
}
