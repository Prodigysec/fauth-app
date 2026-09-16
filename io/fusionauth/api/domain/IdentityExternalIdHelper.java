package io.fusionauth.api.domain;

import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IdentityExternalIdHelper {
  public static final ExternalIdentifier.ExternalIdType[] identityExternalIdTypes = new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.EmailVerification, ExternalIdentifier.ExternalIdType.PhoneVerification };
  
  public static String getLoginId(ExternalIdentifier paramExternalIdentifier) {
    return Optional.<ExternalIdentifier.ExternalIdData>ofNullable(paramExternalIdentifier.data)
      .map(paramExternalIdData -> paramExternalIdData.getAttribute("loginId"))
      .orElse(null);
  }
  
  public static IdentityType getLoginIdentityType(ExternalIdentifier.ExternalIdType paramExternalIdType) {
    switch (paramExternalIdType) {
      case EmailVerification:
      
      case PhoneVerification:
      
    } 
    return 

      
      null;
  }
  
  public static IdentityType getLoginIdentityType(ExternalIdentifier paramExternalIdentifier) {
    return Optional.<IdentityType>ofNullable(getLoginIdentityType(paramExternalIdentifier.type))

      
      .orElse((paramExternalIdentifier.data != null) ? IdentityType.of(paramExternalIdentifier.data.getAttribute("loginIdType")) : null);
  }
  
  public static Map<UserIdentity, ExternalIdentifier> groupActiveIdentifiersByIdentity(List<ExternalIdentifier> paramList, User paramUser, Tenant paramTenant) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    for (ExternalIdentifier externalIdentifier : paramList) {
      if (externalIdentifier.isExpired(paramTenant))
        continue; 
      Optional optional = paramUser.identities.stream().filter(paramUserIdentity -> matchesIdentity(paramExternalIdentifier, paramUserIdentity.value, paramUserIdentity.type)).filter(UserIdentity::verificationRequired).findFirst();
      optional.ifPresent(paramUserIdentity -> paramMap.put(paramUserIdentity, paramExternalIdentifier));
    } 
    return (Map)hashMap;
  }
  
  public static boolean matchesIdentity(ExternalIdentifier paramExternalIdentifier, String paramString, IdentityType paramIdentityType) {
    String str = getLoginId(paramExternalIdentifier);
    return (getLoginIdentityType(paramExternalIdentifier).is(paramIdentityType) && str != null && str.equals(paramString));
  }
}
