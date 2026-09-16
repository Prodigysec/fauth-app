package io.fusionauth.api.service.moderation.cleanspeak;

import com.inversoft.json.ToString;
import io.fusionauth.domain.HTTPHeaders;
import java.net.URI;
import java.util.Objects;

public class NotificationServer {
  public Integer connectTimeout;
  
  public HTTPHeaders headers = new HTTPHeaders();
  
  public String httpAuthenticationPassword;
  
  public String httpAuthenticationUsername;
  
  public Integer id;
  
  public Integer readTimeout;
  
  public String sslCertificate;
  
  public URI url;
  
  public boolean equals(Object paramObject) {
    NotificationServer notificationServer;
    if (this == paramObject)
      return true; 
    if (paramObject instanceof NotificationServer) {
      notificationServer = (NotificationServer)paramObject;
    } else {
      return false;
    } 
    return (Objects.equals(this.connectTimeout, notificationServer.connectTimeout) && 
      Objects.equals(this.headers, notificationServer.headers) && 
      Objects.equals(this.httpAuthenticationPassword, notificationServer.httpAuthenticationPassword) && 
      Objects.equals(this.httpAuthenticationUsername, notificationServer.httpAuthenticationUsername) && 
      Objects.equals(this.readTimeout, notificationServer.readTimeout) && 
      Objects.equals(this.sslCertificate, notificationServer.sslCertificate) && 
      Objects.equals(this.url, notificationServer.url));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.connectTimeout, this.headers, this.httpAuthenticationPassword, this.httpAuthenticationUsername, this.readTimeout, this.sslCertificate, this.url });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
