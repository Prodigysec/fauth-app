package io.fusionauth.api.domain.api;

import com.inversoft.authentication.api.domain.AuthenticationKey;
import com.inversoft.authentication.api.domain.AuthenticationKeyFormat;
import io.fusionauth.domain.APIKey;

public class APIKeyBridge extends APIKey {
  public static AuthenticationKey convert(APIKey paramAPIKey) {
    if (paramAPIKey == null)
      return null; 
    return (AuthenticationKey)((AuthenticationKey)((AuthenticationKey)((AuthenticationKey)((AuthenticationKey)((AuthenticationKey)((AuthenticationKey)((AuthenticationKey)((AuthenticationKey)((AuthenticationKey)((AuthenticationKey)((AuthenticationKey)(new AuthenticationKey())
      .with(paramAuthenticationKey -> paramAuthenticationKey.id = paramAPIKey.id))
      .with(paramAuthenticationKey -> paramAuthenticationKey.expirationInstant = paramAPIKey.expirationInstant))
      .with(paramAuthenticationKey -> paramAuthenticationKey.insertInstant = paramAPIKey.insertInstant))
      .with(paramAuthenticationKey -> paramAuthenticationKey.ipAccessControlListId = paramAPIKey.ipAccessControlListId))
      .with(paramAuthenticationKey -> paramAuthenticationKey.key = paramAPIKey.key))
      .with(paramAuthenticationKey -> paramAuthenticationKey.keyFormat = paramAPIKey.retrievable ? AuthenticationKeyFormat.None : AuthenticationKeyFormat.SHA256))
      .with(paramAuthenticationKey -> paramAuthenticationKey.keyManager = paramAPIKey.keyManager))
      .with(paramAuthenticationKey -> paramAuthenticationKey.lastUpdateInstant = paramAPIKey.lastUpdateInstant))
      .with(paramAuthenticationKey -> paramAuthenticationKey.metaData = (paramAPIKey.metaData != null) ? new AuthenticationKey.AuthenticationMetaData(paramAPIKey.metaData.attributes) : null))
      .with(paramAuthenticationKey -> paramAuthenticationKey.name = paramAPIKey.name))
      .with(paramAuthenticationKey -> paramAuthenticationKey.permissions = (paramAPIKey.permissions != null) ? new AuthenticationKey.AuthenticationPermissions(paramAPIKey.permissions.endpoints) : null))
      .with(paramAuthenticationKey -> paramAuthenticationKey.tenantId = paramAPIKey.tenantId);
  }
  
  public static APIKey convert(AuthenticationKey paramAuthenticationKey) {
    if (paramAuthenticationKey == null)
      return null; 
    return (new APIKey())
      .with(paramAPIKey -> paramAPIKey.id = paramAuthenticationKey.id)
      .with(paramAPIKey -> paramAPIKey.expirationInstant = paramAuthenticationKey.expirationInstant)
      .with(paramAPIKey -> paramAPIKey.insertInstant = paramAuthenticationKey.insertInstant)
      .with(paramAPIKey -> paramAPIKey.ipAccessControlListId = paramAuthenticationKey.ipAccessControlListId)
      
      .with(paramAPIKey -> paramAPIKey.key = (AuthenticationKeyFormat.SHA256 != paramAuthenticationKey.keyFormat) ? paramAuthenticationKey.key : null)
      .with(paramAPIKey -> paramAPIKey.keyManager = paramAuthenticationKey.keyManager)
      .with(paramAPIKey -> paramAPIKey.lastUpdateInstant = paramAuthenticationKey.lastUpdateInstant)
      .with(paramAPIKey -> paramAPIKey.metaData = (paramAuthenticationKey.metaData != null) ? new APIKey.APIKeyMetaData(paramAuthenticationKey.metaData.attributes) : null)
      .with(paramAPIKey -> paramAPIKey.name = paramAuthenticationKey.name)
      .with(paramAPIKey -> paramAPIKey.permissions = (paramAuthenticationKey.permissions != null) ? new APIKey.APIKeyPermissions(paramAuthenticationKey.permissions.endpoints) : null)
      .with(paramAPIKey -> paramAPIKey.retrievable = (paramAuthenticationKey.keyFormat == null || paramAuthenticationKey.keyFormat == AuthenticationKeyFormat.None))
      .with(paramAPIKey -> paramAPIKey.tenantId = paramAuthenticationKey.tenantId);
  }
  
  public static void copyFromTo(AuthenticationKey paramAuthenticationKey, APIKey paramAPIKey) {
    if (paramAuthenticationKey == null || paramAPIKey == null)
      return; 
    paramAPIKey.id = paramAuthenticationKey.id;
    paramAPIKey.expirationInstant = paramAuthenticationKey.expirationInstant;
    paramAPIKey.insertInstant = paramAuthenticationKey.insertInstant;
    paramAPIKey.ipAccessControlListId = paramAuthenticationKey.ipAccessControlListId;
    if (paramAuthenticationKey.keyFormat != null && paramAuthenticationKey.keyFormat != AuthenticationKeyFormat.SHA256)
      paramAPIKey.key = paramAuthenticationKey.key; 
    paramAPIKey.retrievable = (paramAuthenticationKey.keyFormat == null || paramAuthenticationKey.keyFormat == AuthenticationKeyFormat.None);
    paramAPIKey.metaData = (paramAuthenticationKey.metaData != null) ? new APIKey.APIKeyMetaData(paramAuthenticationKey.metaData.attributes) : null;
    paramAPIKey.name = paramAuthenticationKey.name;
    paramAPIKey.lastUpdateInstant = paramAuthenticationKey.lastUpdateInstant;
    paramAPIKey.permissions = (paramAuthenticationKey.permissions != null) ? new APIKey.APIKeyPermissions(paramAuthenticationKey.permissions.endpoints) : null;
    paramAPIKey.keyManager = paramAuthenticationKey.keyManager;
    paramAPIKey.tenantId = paramAuthenticationKey.tenantId;
  }
}
