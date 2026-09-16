package io.fusionauth.api.util;

import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class UserTools {
  public static String userAndRegistrationApplications(User paramUser) {
    if (paramUser == null)
      return ""; 
    List<UserRegistration> list = paramUser.getRegistrations();
    String str = list.stream().map(paramUserRegistration -> paramUserRegistration.applicationId.toString()).collect(Collectors.joining(","));
    return "user: " + String.valueOf(paramUser.id) + " with " + list.size() + " registrations for applications: " + str;
  }
  
  public static boolean isMemberOfGroup(@Nonnull User paramUser, @Nonnull UUID paramUUID) {
    return paramUser.getMemberships().stream().anyMatch(paramGroupMember -> paramUUID.equals(paramGroupMember.groupId));
  }
}
