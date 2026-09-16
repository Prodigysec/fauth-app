package io.fusionauth.domain.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.Enableable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class BaseIdentityProviderApplicationConfiguration extends Enableable implements JSONColumnable {
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public final Map<String, Object> data = new HashMap<>();
  
  @JSONColumn
  public boolean createRegistration = true;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof BaseIdentityProviderApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    BaseIdentityProviderApplicationConfiguration baseIdentityProviderApplicationConfiguration = (BaseIdentityProviderApplicationConfiguration)paramObject;
    return (this.createRegistration == baseIdentityProviderApplicationConfiguration.createRegistration && 
      Objects.equals(this.data, baseIdentityProviderApplicationConfiguration.data));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.data, Boolean.valueOf(this.createRegistration) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
