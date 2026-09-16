package io.fusionauth.domain.messenger;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskMapValue;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.HTTPHeaders;
import java.net.URI;
import java.util.Objects;

public class GenericMessengerConfiguration extends BaseMessengerConfiguration implements Buildable<GenericMessengerConfiguration> {
  @JSONColumn
  public Integer connectTimeout;
  
  @MaskMapValue(maskAll = true)
  @JSONColumn
  public HTTPHeaders headers = new HTTPHeaders();
  
  @MaskString
  @JSONColumn
  public String httpAuthenticationPassword;
  
  @JSONColumn
  public String httpAuthenticationUsername;
  
  @JSONColumn
  public Integer readTimeout;
  
  @JSONColumn
  public String sslCertificate;
  
  @JSONColumn
  public URI url;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    GenericMessengerConfiguration genericMessengerConfiguration = (GenericMessengerConfiguration)paramObject;
    return (Objects.equals(this.connectTimeout, genericMessengerConfiguration.connectTimeout) && 
      Objects.equals(this.headers, genericMessengerConfiguration.headers) && 
      Objects.equals(this.httpAuthenticationPassword, genericMessengerConfiguration.httpAuthenticationPassword) && 
      Objects.equals(this.httpAuthenticationUsername, genericMessengerConfiguration.httpAuthenticationUsername) && 
      Objects.equals(this.readTimeout, genericMessengerConfiguration.readTimeout) && 
      Objects.equals(this.sslCertificate, genericMessengerConfiguration.sslCertificate) && 
      Objects.equals(this.url, genericMessengerConfiguration.url));
  }
  
  public MessengerType getType() {
    return MessengerType.Generic;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.connectTimeout, this.headers, this.httpAuthenticationPassword, this.httpAuthenticationUsername, this.readTimeout, this.sslCertificate, this.url });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
