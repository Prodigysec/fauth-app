package io.fusionauth.api.scim;

import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.User;
import io.fusionauth.scim.domain.SCIMEnterpriseUser;
import io.fusionauth.scim.domain.SCIMGroup;
import io.fusionauth.scim.domain.SCIMMember;
import io.fusionauth.scim.domain.SCIMMeta;
import io.fusionauth.scim.domain.SCIMUser;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SCIMConverter {
  public static SCIMEnterpriseUser toEnterpriseUser(User paramUser, String paramString1, String paramString2) {
    return (SCIMEnterpriseUser)((SCIMEnterpriseUser)((SCIMEnterpriseUser)((SCIMEnterpriseUser)((SCIMEnterpriseUser)(new SCIMEnterpriseUser())
      .with(paramSCIMEnterpriseUser -> paramSCIMEnterpriseUser.active = Boolean.valueOf(paramUser.active)))
      .with(paramSCIMEnterpriseUser -> paramSCIMEnterpriseUser.externalId = paramString))
      .with(paramSCIMEnterpriseUser -> paramSCIMEnterpriseUser.id = paramUser.id))
      .with(paramSCIMEnterpriseUser -> paramSCIMEnterpriseUser.schemas = Arrays.asList(new String[] { "urn:ietf:params:scim:schemas:core:2.0:User", "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User" }))).with(paramSCIMEnterpriseUser -> paramSCIMEnterpriseUser.meta = (SCIMMeta)((SCIMMeta)((SCIMMeta)((SCIMMeta)(new SCIMMeta()).with(())).with(())).with(())).with(()));
  }
  
  public static SCIMGroup toSCIMGroup(Group paramGroup, List<GroupMember> paramList, Map<UUID, User> paramMap, String paramString1, String paramString2) {
    SCIMGroup sCIMGroup = (SCIMGroup)((SCIMGroup)((SCIMGroup)((SCIMGroup)((SCIMGroup)(new SCIMGroup()).with(paramSCIMGroup -> paramSCIMGroup.id = paramGroup.id)).with(paramSCIMGroup -> paramSCIMGroup.externalId = paramString)).with(paramSCIMGroup -> paramSCIMGroup.schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:Group"))).with(paramSCIMGroup -> paramSCIMGroup.meta = (SCIMMeta)((SCIMMeta)((SCIMMeta)((SCIMMeta)(new SCIMMeta()).with(())).with(())).with(())).with(()))).with(paramSCIMGroup -> paramSCIMGroup.members = Collections.emptyList());
    if (paramList != null && !paramList.isEmpty())
      sCIMGroup




        
        .members = (List)paramList.stream().filter(paramGroupMember -> paramMap.containsKey(paramGroupMember.userId)).map(paramGroupMember -> (SCIMMember)((SCIMMember)((SCIMMember)(new SCIMMember()).with(())).with(())).with(())).collect(Collectors.toList()); 
    return sCIMGroup;
  }
  
  public static SCIMUser toSCIMUser(User paramUser, String paramString1, String paramString2) {
    return (SCIMUser)((SCIMUser)((SCIMUser)((SCIMUser)((SCIMUser)(new SCIMUser())
      .with(paramSCIMUser -> paramSCIMUser.active = Boolean.valueOf(paramUser.active)))
      .with(paramSCIMUser -> paramSCIMUser.id = paramUser.id))
      .with(paramSCIMUser -> paramSCIMUser.externalId = paramString))
      .with(paramSCIMUser -> paramSCIMUser.schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:User")))
      .with(paramSCIMUser -> paramSCIMUser.meta = (SCIMMeta)((SCIMMeta)((SCIMMeta)((SCIMMeta)(new SCIMMeta()).with(())).with(())).with(())).with(()));
  }
}
