package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;

public class TwitterApplicationConfiguration extends BaseIdentityProviderApplicationConfiguration implements Buildable<TwitterApplicationConfiguration> {
  @JSONColumn
  public String buttonText;
  
  @JSONColumn
  public String consumerKey;
  
  @MaskString
  @JSONColumn
  public String consumerSecret;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TwitterApplicationConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    TwitterApplicationConfiguration twitterApplicationConfiguration = (TwitterApplicationConfiguration)paramObject;
    return (Objects.equals(this.buttonText, twitterApplicationConfiguration.buttonText) && 
      Objects.equals(this.consumerKey, twitterApplicationConfiguration.consumerKey) && 
      Objects.equals(this.consumerSecret, twitterApplicationConfiguration.consumerSecret));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonText, this.consumerKey, this.consumerSecret });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
