package io.fusionauth.api.service.user;

import io.fusionauth.api.util.PhoneNumberTools;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.oauth2.UserState;
import io.fusionauth.domain.util.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class IdentityHelper {
  public static String canonicalizeValue(UserIdentity paramUserIdentity) {
    return canonicalizeValue(paramUserIdentity.value, paramUserIdentity.type);
  }
  
  public static String canonicalizeValue(String paramString, IdentityType paramIdentityType) {
    if (paramIdentityType.is(IdentityType.email))
      return Normalizer.toLowerCase(paramString); 
    if (paramIdentityType.is(IdentityType.phoneNumber))
      return Optional.<String>ofNullable(PhoneNumberTools.safeToE164format(paramString))
        
        .orElse(paramString); 
    return paramString;
  }
  
  public static UserState getUserStateFromUserAndRegistration(User paramUser, UserRegistration paramUserRegistration) {
    if (paramUserRegistration == null)
      return UserState.AuthenticatedNotRegistered; 
    if (!paramUserRegistration.verified)
      return UserState.AuthenticatedRegistrationNotVerified; 
    Objects.requireNonNull(paramUser);
    List list = IdentityTypeValidator.VerifiableIdentityTypes.stream().map(paramUser::resolvePrimaryIdentity).filter(Objects::nonNull).toList();
    if (!list.isEmpty())
      return (list.stream().anyMatch(paramUserIdentity -> paramUserIdentity.verified) || list
        .stream().noneMatch(UserIdentity::verificationRequired)) ? 
        UserState.Authenticated : 
        UserState.AuthenticatedNotVerified; 
    return UserState.Authenticated;
  }
  
  public static UserIdentity resolveIdentity(User paramUser, String paramString, List<IdentityType> paramList) {
    if (paramString == null)
      return null; 
    if (paramList == null || paramList.isEmpty())
      paramList = DefaultUserReaderService.DefaultIdentityTypes; 
    for (IdentityType identityType : paramList) {
      UserIdentity userIdentity = paramUser.identities.stream().filter(paramUserIdentity -> paramUserIdentity.type.is(paramIdentityType)).filter(paramUserIdentity -> {
            String str = canonicalizeValue(paramString, paramUserIdentity.type).toLowerCase();
            return str.equals(paramUserIdentity.value.toLowerCase());
          }).findFirst().orElse(null);
      if (userIdentity != null)
        return userIdentity; 
    } 
    return null;
  }
  
  public static UserIdentity resolveIdentity(User paramUser, String paramString, IdentityType paramIdentityType) {
    List<IdentityType> list = null;
    if (paramIdentityType != null)
      list = Arrays.asList(new IdentityType[] { paramIdentityType }); 
    return resolveIdentity(paramUser, paramString, list);
  }
}
