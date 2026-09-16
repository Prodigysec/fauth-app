package io.fusionauth.app.service.scim;

import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import io.fusionauth.scim.domain.BaseSCIMResource;
import io.fusionauth.scim.domain.SCIMEnterpriseUser;
import io.fusionauth.scim.domain.SCIMGroup;
import io.fusionauth.scim.domain.SCIMPatchRequest;
import io.fusionauth.scim.domain.SCIMResource;
import io.fusionauth.scim.domain.SCIMResponse;
import io.fusionauth.scim.domain.SCIMUser;
import java.util.List;
import java.util.UUID;

public interface SCIMFrontendService {
  <T> T applyPatchRequest(SCIMPatchRequest paramSCIMPatchRequest, BaseSCIMResource<T> paramBaseSCIMResource);
  
  SCIMResponse createEnterpriseUser(Tenant paramTenant, UUID paramUUID, SCIMEnterpriseUser paramSCIMEnterpriseUser, String paramString, EventInfo paramEventInfo);
  
  SCIMResponse createGroup(Tenant paramTenant, UUID paramUUID1, SCIMGroup paramSCIMGroup, UUID paramUUID2, String paramString, EventInfo paramEventInfo);
  
  SCIMResponse createUser(Tenant paramTenant, UUID paramUUID, SCIMUser paramSCIMUser, String paramString, EventInfo paramEventInfo);
  
  void deleteGroup(Tenant paramTenant, UUID paramUUID);
  
  void deleteUser(Tenant paramTenant, UUID paramUUID, EventInfo paramEventInfo);
  
  SCIMResource getSchemaResourceById(Tenant paramTenant, String paramString1, String paramString2);
  
  List<SCIMResource> getSchemaResources(Tenant paramTenant, String paramString);
  
  SCIMResponse patchEnterpriseUser(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, SCIMPatchRequest paramSCIMPatchRequest, String paramString);
  
  SCIMResponse patchGroup(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, SCIMPatchRequest paramSCIMPatchRequest, String paramString, EventInfo paramEventInfo);
  
  SCIMResponse patchUser(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, SCIMPatchRequest paramSCIMPatchRequest, String paramString);
  
  SCIMResponse retrieveEnterpriseUserById(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString);
  
  SCIMResponse retrieveGroupById(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString);
  
  SCIMResponse retrieveUserById(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString);
  
  SCIMResponse searchEnterpriseUsers(Tenant paramTenant, UUID paramUUID, String paramString1, int paramInt1, int paramInt2, String paramString2);
  
  SCIMResponse searchGroups(Tenant paramTenant, UUID paramUUID, String paramString1, int paramInt1, int paramInt2, String paramString2);
  
  SCIMResponse searchUsers(Tenant paramTenant, UUID paramUUID, String paramString1, int paramInt1, int paramInt2, String paramString2);
  
  SCIMResponse updateEnterpriseUser(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, SCIMEnterpriseUser paramSCIMEnterpriseUser, String paramString, EventInfo paramEventInfo);
  
  SCIMResponse updateGroup(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, SCIMGroup paramSCIMGroup, String paramString, EventInfo paramEventInfo);
  
  SCIMResponse updateUser(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, SCIMUser paramSCIMUser, String paramString, EventInfo paramEventInfo);
}
