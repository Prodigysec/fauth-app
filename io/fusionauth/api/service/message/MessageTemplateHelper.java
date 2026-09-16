package io.fusionauth.api.service.message;

import io.fusionauth.api.service.TemplateHelper;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import java.util.HashMap;
import java.util.Map;

public class MessageTemplateHelper {
  public static Map<String, Object> getBaseParameters(Tenant paramTenant, Application paramApplication, User paramUser, String paramString) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("tenant", (new Tenant(paramTenant)).secure());
    hashMap.put("baseUrl", TemplateHelper.resolveBaseUrl(paramTenant, paramApplication));
    if (paramApplication != null)
      hashMap.put("application", (new Application(paramApplication)).secure()); 
    if (paramString != null)
      hashMap.put("phoneNumber", paramString); 
    if (paramUser != null)
      hashMap.put("user", (new User(paramUser)).secure().sort()); 
    return (Map)hashMap;
  }
}
