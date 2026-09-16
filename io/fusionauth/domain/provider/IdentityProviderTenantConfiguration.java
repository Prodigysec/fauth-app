package io.fusionauth.domain.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.Buildable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IdentityProviderTenantConfiguration implements Buildable<IdentityProviderTenantConfiguration>, JSONColumnable {
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public final Map<String, Object> data = new HashMap<>();
  
  @JSONColumn
  public IdentityProviderLimitUserLinkingPolicy limitUserLinkCount = new IdentityProviderLimitUserLinkingPolicy();
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof IdentityProviderTenantConfiguration))
      return false; 
    IdentityProviderTenantConfiguration identityProviderTenantConfiguration = (IdentityProviderTenantConfiguration)paramObject;
    return (Objects.equals(this.data, identityProviderTenantConfiguration.data) && Objects.equals(this.limitUserLinkCount, identityProviderTenantConfiguration.limitUserLinkCount));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.data, this.limitUserLinkCount });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
