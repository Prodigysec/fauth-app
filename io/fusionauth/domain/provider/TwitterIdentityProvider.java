package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.util.Objects;
import java.util.UUID;

public class TwitterIdentityProvider extends BaseIdentityProvider<TwitterApplicationConfiguration> implements Buildable<TwitterIdentityProvider>, SupportsPostBindings {
  @JSONColumn
  public String buttonText = "Login with Twitter";
  
  @JSONColumn
  public String consumerKey;
  
  @MaskString
  @JSONColumn
  public String consumerSecret;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TwitterIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    TwitterIdentityProvider twitterIdentityProvider = (TwitterIdentityProvider)paramObject;
    return (Objects.equals(this.buttonText, twitterIdentityProvider.buttonText) && 
      Objects.equals(this.consumerKey, twitterIdentityProvider.consumerKey) && 
      Objects.equals(this.consumerSecret, twitterIdentityProvider.consumerSecret));
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.Twitter;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonText, this.consumerKey, this.consumerSecret });
  }
  
  public String lookupButtonText(UUID paramUUID) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupButtonText(String paramString) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramString, ()));
  }
  
  public String lookupConsumerKey(UUID paramUUID) {
    return (String)lookup(() -> this.consumerKey, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupConsumerKey(String paramString) {
    return (String)lookup(() -> this.consumerKey, () -> (String)app(paramString, ()));
  }
  
  public String lookupConsumerSecret(UUID paramUUID) {
    return (String)lookup(() -> this.consumerSecret, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupConsumerSecret(String paramString) {
    return (String)lookup(() -> this.consumerSecret, () -> (String)app(paramString, ()));
  }
  
  public boolean postRequestEnabled() {
    return false;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
