package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.domain.Buildable;
import java.net.URI;
import java.util.Objects;

public class SAMLv2ApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<SAMLv2ApplicationConfiguration> {
  @JSONColumn
  public URI buttonImageURL;
  
  @JSONColumn
  public String buttonText;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof SAMLv2ApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    SAMLv2ApplicationConfiguration sAMLv2ApplicationConfiguration = (SAMLv2ApplicationConfiguration)paramObject;
    return (Objects.equals(this.buttonImageURL, sAMLv2ApplicationConfiguration.buttonImageURL) && 
      Objects.equals(this.buttonText, sAMLv2ApplicationConfiguration.buttonText));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonImageURL, this.buttonText });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
