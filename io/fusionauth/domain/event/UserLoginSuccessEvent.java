package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.util.Objects;
import java.util.UUID;

public class UserLoginSuccessEvent extends BaseUserEvent implements Buildable<UserLoginSuccessEvent> {
  public UUID applicationId;
  
  public String authenticationType;
  
  public UUID connectorId;
  
  public UUID identityProviderId;
  
  public String identityProviderName;
  
  @Deprecated
  public String ipAddress;
  
  @JacksonConstructor
  public UserLoginSuccessEvent() {}
  
  public UserLoginSuccessEvent(EventInfo paramEventInfo, UUID paramUUID, String paramString, BaseIdentityProvider<?> paramBaseIdentityProvider, User paramUser) {
    super(paramEventInfo, paramUser);
    this.applicationId = paramUUID;
    this.authenticationType = paramString;
    this.connectorId = BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID;
    this.identityProviderId = paramBaseIdentityProvider.id;
    this.identityProviderName = paramBaseIdentityProvider.name;
    if (paramEventInfo != null && paramEventInfo.ipAddress != null)
      this.ipAddress = paramEventInfo.ipAddress; 
  }
  
  public UserLoginSuccessEvent(EventInfo paramEventInfo, UUID paramUUID1, UUID paramUUID2, String paramString, User paramUser) {
    super(paramEventInfo, paramUser);
    this.applicationId = paramUUID1;
    this.authenticationType = paramString;
    this.connectorId = paramUUID2;
    if (paramEventInfo != null && paramEventInfo.ipAddress != null)
      this.ipAddress = paramEventInfo.ipAddress; 
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserLoginSuccessEvent userLoginSuccessEvent = (UserLoginSuccessEvent)paramObject;
    return (Objects.equals(this.applicationId, userLoginSuccessEvent.applicationId) && 
      Objects.equals(this.authenticationType, userLoginSuccessEvent.authenticationType) && 
      Objects.equals(this.connectorId, userLoginSuccessEvent.connectorId) && 
      Objects.equals(this.identityProviderId, userLoginSuccessEvent.identityProviderId) && 
      Objects.equals(this.identityProviderName, userLoginSuccessEvent.identityProviderName) && 
      Objects.equals(this.ipAddress, userLoginSuccessEvent.ipAddress));
  }
  
  public EventType getType() {
    return EventType.UserLoginSuccess;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.authenticationType, this.connectorId, this.identityProviderId, this.identityProviderName, this.ipAddress });
  }
}
