package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.BaseCacheLoader;
import com.inversoft.cache.Cache;
import io.fusionauth.api.domain.IPAccessControlListMapper;
import io.fusionauth.api.domain.ip.acl.CompiledIPAccessControlList;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class IPAccessControlListCacheLoader extends BaseCacheLoader<UUID, CompiledIPAccessControlList> implements Runnable {
  private final Cache<UUID, CompiledIPAccessControlList> cache;
  
  private final IPAccessControlListMapper ipAccessControlListMapper;
  
  private final ReactorStatusService reactorStatusService;
  
  @Inject
  public IPAccessControlListCacheLoader(IPAccessControlListCache paramIPAccessControlListCache, IPAccessControlListMapper paramIPAccessControlListMapper, ReactorStatusService paramReactorStatusService) {
    super(Collections.emptyList());
    this.cache = (Cache<UUID, CompiledIPAccessControlList>)paramIPAccessControlListCache;
    this.ipAccessControlListMapper = paramIPAccessControlListMapper;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public void run() {
    load();
  }
  
  protected void internalLoad(Cache<UUID, CompiledIPAccessControlList> paramCache) {
    boolean bool = ReactorStatusValidator.isLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.threatDetection);
    if (bool) {
      paramCache.replace((Map)this.ipAccessControlListMapper.retrieveAll()
          .stream()
          .collect(Collectors.toMap(paramIPAccessControlList -> paramIPAccessControlList.id, paramIPAccessControlList -> new CompiledIPAccessControlList(paramIPAccessControlList.entries))));
    } else {
      paramCache.replace(new HashMap<>());
    } 
  }
  
  protected Cache<UUID, CompiledIPAccessControlList> resolveCache() {
    return this.cache;
  }
}
