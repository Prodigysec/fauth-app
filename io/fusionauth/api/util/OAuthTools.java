package io.fusionauth.api.util;

import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OAuthTools {
  public static String convertMaxAgeToPrompt(String paramString, Long paramLong, SSOService.SSOSession paramSSOSession) {
    Set<String> set = (paramString != null) ? ParameterTools.splitSpaceSeparated(paramString) : new LinkedHashSet();
    boolean bool = forceLoginRequested(set, paramLong, paramSSOSession);
    if (bool && !set.contains("login")) {
      set.add("login");
      return String.join(" ", (Iterable)set);
    } 
    return paramString;
  }
  
  public static boolean forceLoginRequested(Set<String> paramSet, Long paramLong, SSOService.SSOSession paramSSOSession) {
    Objects.requireNonNull(paramSet);
    Objects.requireNonNull(paramSSOSession);
    if (paramSet.contains("login"))
      return true; 
    if (paramLong != null) {
      if (paramLong.longValue() == 0L)
        return true; 
      if (paramSSOSession.user == null)
        return true; 
      if (paramSSOSession.refreshToken.metaData == null || paramSSOSession.refreshToken.metaData.data == null)
        return false; 
      Object object = paramSSOSession.refreshToken.metaData.data.get("auth_time");
      if (object != null)
        return Instant.ofEpochSecond(Long.parseLong(object.toString()) + paramLong.longValue()).isBefore(Instant.now()); 
    } 
    return false;
  }
  
  public static boolean isCompleteRegistrationAllowed(@Nonnull Application paramApplication, @Nullable UserRegistration paramUserRegistration) {
    if (paramApplication.registrationConfiguration.enabled)
      return true; 
    return (paramUserRegistration != null && paramApplication.registrationConfiguration.completeRegistration);
  }
  
  public static boolean isCompleteRegistrationAllowed(@Nonnull Application paramApplication, @Nullable User paramUser) {
    return (paramUser != null && isCompleteRegistrationAllowed(paramApplication, paramUser.getRegistrationForApplication(paramApplication.id)));
  }
}
