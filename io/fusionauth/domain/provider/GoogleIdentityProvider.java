package io.fusionauth.domain.provider;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.util.Normalizer;
import java.io.StringReader;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

public class GoogleIdentityProvider extends BaseIdentityProvider<GoogleApplicationConfiguration> implements Buildable<GoogleIdentityProvider>, SupportsPostBindings {
  @JSONColumn
  public String buttonText = "Login with Google";
  
  @JSONColumn
  public String client_id;
  
  @MaskString
  @JSONColumn
  public String client_secret;
  
  @JSONColumn
  public IdentityProviderLoginMethod loginMethod;
  
  @JSONColumn
  public GoogleIdentityProviderProperties properties = new GoogleIdentityProviderProperties("# Omit the data- prefix\nauto_prompt=true\nauto_select=false\ncancel_on_tap_outside=false\ncontext=signin\nitp_support=true", "# Omit the data- prefix\nlogo_alignment=left\nshape=rectangular\nsize=large\ntext=signin_with\ntheme=outline\ntype=standard");
  
  @JSONColumn
  public String scope;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof GoogleIdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    GoogleIdentityProvider googleIdentityProvider = (GoogleIdentityProvider)paramObject;
    return (Objects.equals(this.buttonText, googleIdentityProvider.buttonText) && 
      Objects.equals(this.client_id, googleIdentityProvider.client_id) && 
      Objects.equals(this.client_secret, googleIdentityProvider.client_secret) && this.loginMethod == googleIdentityProvider.loginMethod && 
      
      Objects.equals(this.properties, googleIdentityProvider.properties) && 
      Objects.equals(this.scope, googleIdentityProvider.scope));
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.Google;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.buttonText, this.client_id, this.client_secret, this.loginMethod, this.properties, this.scope });
  }
  
  public Properties lookupAPIProperties(String paramString) {
    String str = (String)app(paramString, paramGoogleApplicationConfiguration -> paramGoogleApplicationConfiguration.properties.api);
    try {
      Properties properties = new Properties();
      properties.load(new StringReader(this.properties.api));
      if (str != null) {
        Properties properties1 = new Properties();
        properties1.load(new StringReader(str));
        properties.putAll(properties1);
      } 
      return properties;
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public Properties lookupButtonProperties(String paramString) {
    String str = (String)app(paramString, paramGoogleApplicationConfiguration -> paramGoogleApplicationConfiguration.properties.button);
    try {
      Properties properties = new Properties();
      properties.load(new StringReader(this.properties.button));
      if (str != null) {
        Properties properties1 = new Properties();
        properties1.load(new StringReader(str));
        properties.putAll(properties1);
      } 
      return properties;
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public String lookupButtonText(String paramString) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramString, ()));
  }
  
  public String lookupClientId(UUID paramUUID) {
    return (String)lookup(() -> this.client_id, () -> (String)app(paramUUID, ()));
  }
  
  public String lookupClientId(String paramString) {
    return (String)lookup(() -> this.client_id, () -> (String)app(paramString, ()));
  }
  
  public String lookupClientSecret(UUID paramUUID) {
    return (String)lookup(() -> this.client_secret, () -> (String)app(paramUUID, ()));
  }
  
  public IdentityProviderLoginMethod lookupLoginMethod(String paramString) {
    return (IdentityProviderLoginMethod)lookup(() -> this.loginMethod, () -> (IdentityProviderLoginMethod)app(paramString, ()));
  }
  
  public IdentityProviderLoginMethod lookupLoginMethod(UUID paramUUID) {
    return (IdentityProviderLoginMethod)lookup(() -> this.loginMethod, () -> (IdentityProviderLoginMethod)app(paramUUID, ()));
  }
  
  public String lookupScope(String paramString) {
    return (String)lookup(() -> this.scope, () -> (String)app(paramString, ()));
  }
  
  public void normalize() {
    super.normalize();
    if (this.properties.api != null)
      this.properties.api = Normalizer.lineReturns(this.properties.api); 
    if (this.properties.button != null)
      this.properties.button = Normalizer.lineReturns(this.properties.button); 
    for (GoogleApplicationConfiguration googleApplicationConfiguration : this.applicationConfiguration.values()) {
      if (googleApplicationConfiguration.properties.api != null)
        googleApplicationConfiguration.properties.api = Normalizer.lineReturns(googleApplicationConfiguration.properties.api); 
      if (googleApplicationConfiguration.properties.button != null)
        googleApplicationConfiguration.properties.button = Normalizer.lineReturns(googleApplicationConfiguration.properties.button); 
    } 
  }
  
  public boolean postRequestEnabled() {
    return false;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
