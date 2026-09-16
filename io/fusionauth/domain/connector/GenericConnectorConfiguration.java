package io.fusionauth.domain.connector;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskMapValue;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.HTTPHeaders;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class GenericConnectorConfiguration extends BaseConnectorConfiguration implements Buildable<GenericConnectorConfiguration> {
  @JSONColumn
  public URI authenticationURL;
  
  @JSONColumn
  public int connectTimeout;
  
  @MaskMapValue(maskAll = true)
  @JSONColumn
  public HTTPHeaders headers = new HTTPHeaders();
  
  @MaskString
  @JSONColumn
  public String httpAuthenticationPassword;
  
  @JSONColumn
  public String httpAuthenticationUsername;
  
  @JSONColumn
  public int readTimeout;
  
  public UUID sslCertificateKeyId;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof GenericConnectorConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    GenericConnectorConfiguration genericConnectorConfiguration = (GenericConnectorConfiguration)paramObject;
    return (this.connectTimeout == genericConnectorConfiguration.connectTimeout && this.readTimeout == genericConnectorConfiguration.readTimeout && 
      
      Objects.equals(this.authenticationURL, genericConnectorConfiguration.authenticationURL) && 
      Objects.equals(this.headers, genericConnectorConfiguration.headers) && 
      Objects.equals(this.httpAuthenticationPassword, genericConnectorConfiguration.httpAuthenticationPassword) && 
      Objects.equals(this.httpAuthenticationUsername, genericConnectorConfiguration.httpAuthenticationUsername) && 
      Objects.equals(this.sslCertificateKeyId, genericConnectorConfiguration.sslCertificateKeyId));
  }
  
  public ConnectorType getType() {
    return ConnectorType.Generic;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.authenticationURL, Integer.valueOf(this.connectTimeout), this.headers, this.httpAuthenticationPassword, this.httpAuthenticationUsername, Integer.valueOf(this.readTimeout), this.sslCertificateKeyId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
