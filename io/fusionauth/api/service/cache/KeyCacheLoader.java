package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.BaseCacheLoader;
import com.inversoft.cache.Cache;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.entity.EntityService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.KeyHelper;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.service.system.KeyService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.JWTConfiguration;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.jwt.Verifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class KeyCacheLoader extends BaseCacheLoader<UUID, Key> implements Runnable {
  private final ApplicationReaderService applicationReader;
  
  private final KeyCache cache;
  
  private final EntityService entityService;
  
  private final KeyReaderService keyReader;
  
  private final TenantReaderService tenantReader;
  
  @Inject
  public KeyCacheLoader(ApplicationReaderService paramApplicationReaderService, KeyCache paramKeyCache, KeyReaderService paramKeyReaderService, EntityService paramEntityService, TenantReaderService paramTenantReaderService) {
    super(Collections.emptyList());
    this.applicationReader = paramApplicationReaderService;
    this.cache = paramKeyCache;
    this.entityService = paramEntityService;
    this.keyReader = paramKeyReaderService;
    this.tenantReader = paramTenantReaderService;
  }
  
  public void run() {
    load();
  }
  
  protected void internalLoad(Cache<UUID, Key> paramCache) {
    Map<UUID, Key> map = (Map)this.keyReader.retrieveAll().stream().filter(paramKey -> !paramKey.isExpired()).filter(paramKey -> !paramKey.privateKeyOnly()).collect(Collectors.toMap(paramKey -> paramKey.id, paramKey -> paramKey));
    HashMap<Object, Object> hashMap = new HashMap<>();
    HashSet<UUID> hashSet = new HashSet();
    List<Application> list = this.applicationReader.retrieveAll(null, Collections.emptySet());
    Map map1 = (Map)this.tenantReader.retrieveAll().stream().collect(Collectors.toMap(paramTenant -> paramTenant.id, paramTenant -> paramTenant));
    List<EntityType> list1 = this.entityService.retrieveAllTypes();
    map1.values().forEach(paramTenant -> addJwtKeyIds(paramSet, paramTenant.jwtConfiguration));
    for (EntityType entityType : list1) {
      if (entityType.jwtConfiguration.enabled)
        addJwtKeyIds(hashSet, entityType.jwtConfiguration); 
    } 
    for (Application application : list) {
      Tenant tenant = (Tenant)map1.get(application.tenantId);
      JWTConfiguration jWTConfiguration = (tenant != null) ? tenant.lookupJWTConfiguration(application) : application.jwtConfiguration;
      if (jWTConfiguration != null)
        addJwtKeyIds(hashSet, jWTConfiguration); 
    } 
    for (Key key : map.values()) {
      if (KeyService.ClientSecretShadowKeys.contains(key.id))
        continue; 
      if (!hashSet.contains(key.id))
        continue; 
      if (key.type == Key.KeyType.Secret)
        continue; 
      Verifier verifier = KeyHelper.getVerifier(key);
      if (verifier != null)
        hashMap.put(key.kid, verifier); 
    } 
    this.cache.replaceAll(map, (Map)hashMap);
  }
  
  protected Cache<UUID, Key> resolveCache() {
    return (Cache<UUID, Key>)this.cache;
  }
  
  private void addJwtKeyIds(Set<UUID> paramSet, JWTConfiguration paramJWTConfiguration) {
    if (paramJWTConfiguration == null)
      return; 
    if (paramJWTConfiguration.accessTokenKeyId != null)
      paramSet.add(paramJWTConfiguration.accessTokenKeyId); 
    paramSet.addAll(paramJWTConfiguration.accessTokenVerificationKeyIds);
    if (paramJWTConfiguration.idTokenKeyId != null)
      paramSet.add(paramJWTConfiguration.idTokenKeyId); 
    paramSet.addAll(paramJWTConfiguration.idTokenVerificationKeyIds);
  }
  
  private void addJwtKeyIds(Set<UUID> paramSet, EntityType.EntityJWTConfiguration paramEntityJWTConfiguration) {
    if (paramEntityJWTConfiguration.accessTokenKeyId != null)
      paramSet.add(paramEntityJWTConfiguration.accessTokenKeyId); 
    paramSet.addAll(paramEntityJWTConfiguration.accessTokenVerificationKeyIds);
  }
}
