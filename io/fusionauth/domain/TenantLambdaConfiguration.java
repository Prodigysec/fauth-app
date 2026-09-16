package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;
import java.util.UUID;

public class TenantLambdaConfiguration implements Buildable<TenantLambdaConfiguration> {
  public UUID loginValidationId;
  
  public UUID multiFactorRequirementId;
  
  public UUID scimEnterpriseUserRequestConverterId;
  
  public UUID scimEnterpriseUserResponseConverterId;
  
  public UUID scimGroupRequestConverterId;
  
  public UUID scimGroupResponseConverterId;
  
  public UUID scimUserRequestConverterId;
  
  public UUID scimUserResponseConverterId;
  
  @JacksonConstructor
  public TenantLambdaConfiguration() {}
  
  public TenantLambdaConfiguration(TenantLambdaConfiguration paramTenantLambdaConfiguration) {
    this.loginValidationId = paramTenantLambdaConfiguration.loginValidationId;
    this.multiFactorRequirementId = paramTenantLambdaConfiguration.multiFactorRequirementId;
    this.scimEnterpriseUserRequestConverterId = paramTenantLambdaConfiguration.scimEnterpriseUserRequestConverterId;
    this.scimEnterpriseUserResponseConverterId = paramTenantLambdaConfiguration.scimEnterpriseUserResponseConverterId;
    this.scimGroupRequestConverterId = paramTenantLambdaConfiguration.scimGroupRequestConverterId;
    this.scimGroupResponseConverterId = paramTenantLambdaConfiguration.scimGroupResponseConverterId;
    this.scimUserRequestConverterId = paramTenantLambdaConfiguration.scimUserRequestConverterId;
    this.scimUserResponseConverterId = paramTenantLambdaConfiguration.scimUserResponseConverterId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TenantLambdaConfiguration))
      return false; 
    TenantLambdaConfiguration tenantLambdaConfiguration = (TenantLambdaConfiguration)paramObject;
    return (Objects.equals(this.loginValidationId, tenantLambdaConfiguration.loginValidationId) && 
      Objects.equals(this.multiFactorRequirementId, tenantLambdaConfiguration.multiFactorRequirementId) && 
      Objects.equals(this.scimEnterpriseUserRequestConverterId, tenantLambdaConfiguration.scimEnterpriseUserRequestConverterId) && 
      Objects.equals(this.scimEnterpriseUserResponseConverterId, tenantLambdaConfiguration.scimEnterpriseUserResponseConverterId) && 
      Objects.equals(this.scimGroupRequestConverterId, tenantLambdaConfiguration.scimGroupRequestConverterId) && 
      Objects.equals(this.scimGroupResponseConverterId, tenantLambdaConfiguration.scimGroupResponseConverterId) && 
      Objects.equals(this.scimUserRequestConverterId, tenantLambdaConfiguration.scimUserRequestConverterId) && 
      Objects.equals(this.scimUserResponseConverterId, tenantLambdaConfiguration.scimUserResponseConverterId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.loginValidationId, this.multiFactorRequirementId, this.scimEnterpriseUserRequestConverterId, this.scimEnterpriseUserResponseConverterId, this.scimGroupRequestConverterId, this.scimGroupResponseConverterId, this.scimUserRequestConverterId, this.scimUserResponseConverterId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
