package io.fusionauth.api.service.cache;

import com.inversoft.cache.CloseableReentrantReadWriteLock;
import com.inversoft.cache.SimpleCache;
import io.fusionauth.api.service.authentication.HYPRIdentityProviderAuthenticationService;
import io.fusionauth.api.service.authentication.IdentityProviderAuthenticationService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.util.EmailTools;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProviderApplicationConfiguration;
import io.fusionauth.domain.provider.DomainBasedIdentityProvider;
import io.fusionauth.domain.provider.ExternalJWTIdentityProvider;
import io.fusionauth.domain.provider.HYPRIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IdentityProviderCache extends SimpleCache<UUID, BaseIdentityProvider<?>> {
  private final Map<UUID, List<BaseIdentityProvider<?>>> applicationIdentityProviders = new HashMap<>();
  
  private final Map<DomainIdentityProviderKey, BaseIdentityProvider<?>> domainIdentityProviders = new HashMap<>();
  
  public Map<IdentityProviderType, IdentityProviderAuthenticationService> identityProviderAuthenticationServices;
  
  public List<BaseIdentityProvider<?>> getByApplicationId(@Nullable UUID paramUUID1, UUID paramUUID2) {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.readLock());
    try {
      List list = this.applicationIdentityProviders.get(paramUUID2);
      if (list == null) {
        List<?> list2 = Collections.emptyList();
        closeableLock.close();
        return (List)list2;
      } 
      List<BaseIdentityProvider<?>> list1 = list.stream().filter(paramBaseIdentityProvider -> (paramBaseIdentityProvider.tenantId == null || paramBaseIdentityProvider.tenantId.equals(paramUUID))).toList();
      closeableLock.close();
      return list1;
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public BaseIdentityProvider<?> getByDomain(@Nullable UUID paramUUID, String paramString) {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.readLock());
    try {
      if (paramUUID != null) {
        BaseIdentityProvider<?> baseIdentityProvider1 = this.domainIdentityProviders.get(new DomainIdentityProviderKey(paramUUID, paramString));
        if (baseIdentityProvider1 != null) {
          BaseIdentityProvider<?> baseIdentityProvider2 = baseIdentityProvider1;
          closeableLock.close();
          return baseIdentityProvider2;
        } 
      } 
      BaseIdentityProvider<?> baseIdentityProvider = this.domainIdentityProviders.get(new DomainIdentityProviderKey(null, paramString));
      closeableLock.close();
      return baseIdentityProvider;
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public List<BaseIdentityProvider<?>> getPasswordlessProviders(UUID paramUUID) {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.readLock());
    try {
      List<BaseIdentityProvider<?>> list = (List)((List)this.applicationIdentityProviders.getOrDefault(paramUUID, Collections.emptyList())).stream().filter(paramBaseIdentityProvider -> paramBaseIdentityProvider instanceof io.fusionauth.domain.provider.PasswordlessIdentityProvider).collect(Collectors.toList());
      closeableLock.close();
      return list;
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public boolean isExternalJWTVerificationKey(UUID paramUUID) {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.readLock());
    try {
      Objects.requireNonNull(ExternalJWTIdentityProvider.class);
      Objects.requireNonNull(ExternalJWTIdentityProvider.class);
      boolean bool = this.cache.values().stream().filter(ExternalJWTIdentityProvider.class::isInstance).map(ExternalJWTIdentityProvider.class::cast).anyMatch(paramExternalJWTIdentityProvider -> paramExternalJWTIdentityProvider.verificationKeyIds.contains(paramUUID));
      closeableLock.close();
      return bool;
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public BaseIdentityProvider<?> lookup(@Nullable UUID paramUUID, String paramString) {
    paramString = EmailTools.getEmailDomain(paramString);
    return getByDomain(paramUUID, paramString);
  }
  
  public BaseIdentityProvider<?> lookupPasswordlessProvider(UUID paramUUID, String paramString) {
    for (BaseIdentityProvider<?> baseIdentityProvider : getPasswordlessProviders(paramUUID)) {
      if (baseIdentityProvider instanceof HYPRIdentityProvider)
        try {
          HYPRIdentityProviderAuthenticationService hYPRIdentityProviderAuthenticationService = (HYPRIdentityProviderAuthenticationService)this.identityProviderAuthenticationServices.get(baseIdentityProvider.getType());
          if (hYPRIdentityProviderAuthenticationService.canHandle((HYPRIdentityProvider)baseIdentityProvider, paramUUID, paramString))
            return baseIdentityProvider; 
        } catch (Exception exception) {
          EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to retrieve HYPR devices for [" + paramString + "]", exception));
        }  
    } 
    return null;
  }
  
  public void replace(Map<UUID, BaseIdentityProvider<?>> paramMap) {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.writeLock());
    try {
      this.cache.clear();
      this.cache.putAll(paramMap);
      this.domainIdentityProviders.clear();
      this.applicationIdentityProviders.clear();
      for (BaseIdentityProvider<?> baseIdentityProvider : paramMap.values()) {
        if (baseIdentityProvider instanceof DomainBasedIdentityProvider) {
          DomainBasedIdentityProvider domainBasedIdentityProvider = (DomainBasedIdentityProvider)baseIdentityProvider;
          domainBasedIdentityProvider.getDomains().forEach(paramString -> this.domainIdentityProviders.put(new DomainIdentityProviderKey(paramBaseIdentityProvider.tenantId, paramString), paramBaseIdentityProvider));
        } 
        baseIdentityProvider.applicationConfiguration.forEach((paramUUID, paramBaseIdentityProviderApplicationConfiguration) -> {
              List<BaseIdentityProvider> list = (List)this.applicationIdentityProviders.computeIfAbsent(paramUUID, ());
              list.add(paramBaseIdentityProvider);
            });
      } 
      closeableLock.close();
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public static final class DomainIdentityProviderKey extends Record {
    @Nullable
    private final UUID tenantId;
    
    @Nonnull
    private final String domain;
    
    public DomainIdentityProviderKey(@Nullable UUID param1UUID, @Nonnull String param1String) {
      this.tenantId = param1UUID;
      this.domain = param1String;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/cache/IdentityProviderCache$DomainIdentityProviderKey;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #143	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/cache/IdentityProviderCache$DomainIdentityProviderKey;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #143	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/cache/IdentityProviderCache$DomainIdentityProviderKey;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #143	-> 0
    }
    
    @Nullable
    public UUID tenantId() {
      return this.tenantId;
    }
    
    @Nonnull
    public String domain() {
      return this.domain;
    }
  }
}
