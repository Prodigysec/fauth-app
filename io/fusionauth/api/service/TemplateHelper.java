package io.fusionauth.api.service;

import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;

public class TemplateHelper {
  public static final String USER_KEY = "user";
  
  private static final String DEFAULT_BASE_URL = "http://localhost:9011";
  
  public static Map<String, Object> getBaseParameters(@Nonnull Tenant paramTenant, Application paramApplication, User paramUser) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("tenant", (new Tenant(paramTenant)).secure());
    hashMap.put("baseUrl", resolveBaseUrl(paramTenant, paramApplication));
    if (paramApplication != null)
      hashMap.put("application", (new Application(paramApplication)).secure()); 
    if (paramUser != null)
      hashMap.put("user", (new User(paramUser)).secure().sort()); 
    return (Map)hashMap;
  }
  
  public static Locale getPreferredLanguage(User paramUser, Application paramApplication) {
    List<Locale> list = getPreferredLanguages(paramUser, paramApplication);
    return list.isEmpty() ? null : list.get(0);
  }
  
  public static List<Locale> getPreferredLanguages(User paramUser, Application paramApplication) {
    if (paramApplication == null)
      return paramUser.preferredLanguages; 
    UserRegistration userRegistration = paramUser.getRegistrationForApplication(paramApplication.id);
    if (userRegistration == null)
      return paramUser.preferredLanguages; 
    if (userRegistration.preferredLanguages.isEmpty())
      return paramUser.preferredLanguages; 
    return userRegistration.preferredLanguages;
  }
  
  public static String resolveBaseUrl(Tenant paramTenant, Application paramApplication) {
    if (paramApplication != null && paramApplication.baseURL != null)
      return paramApplication.baseURL.toString(); 
    if (paramTenant.baseURL != null)
      return paramTenant.baseURL.toString(); 
    return "http://localhost:9011";
  }
}
