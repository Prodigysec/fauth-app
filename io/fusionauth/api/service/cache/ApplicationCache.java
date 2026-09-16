package io.fusionauth.api.service.cache;

import com.inversoft.cache.SimpleCache;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ApplicationCache extends SimpleCache<UUID, Application> {
  public Application get(UUID paramUUID1, UUID paramUUID2, BiFunction<UUID, UUID, Application> paramBiFunction) {
    Application application = (Application)get(paramUUID2);
    if (application == null && paramBiFunction != null) {
      application = paramBiFunction.apply(paramUUID1, paramUUID2);
      if (application != null)
        set(paramUUID2, application); 
    } 
    if (application == null || paramUUID1 == null)
      return (application != null) ? new Application(application) : null; 
    return (application.universalConfiguration.universal || application.tenantId.equals(paramUUID1)) ? new Application(application) : null;
  }
  
  public Application get(UUID paramUUID1, UUID paramUUID2) {
    return get(paramUUID1, paramUUID2, (BiFunction<UUID, UUID, Application>)null);
  }
  
  public List<Application> getAllByTenantId(UUID paramUUID) {
    return (List<Application>)this.cache.values().stream().filter(paramApplication -> (paramApplication.tenantId != null && paramApplication.tenantId.equals(paramUUID))).collect(Collectors.toList());
  }
  
  public ApplicationRole getRoleByName(UUID paramUUID1, UUID paramUUID2, String paramString, BiFunction<UUID, UUID, Application> paramBiFunction) {
    Application application = get(paramUUID1, paramUUID2, paramBiFunction);
    if (application == null)
      return null; 
    return application.getRole(paramString);
  }
}
