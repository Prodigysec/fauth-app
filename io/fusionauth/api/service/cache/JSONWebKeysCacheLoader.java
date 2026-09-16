package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.BaseCacheLoader;
import com.inversoft.cache.Cache;
import io.fusionauth.api.service.jwks.JSONWebKeyService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.jwks.JSONWebKeyInfoProvider;
import io.fusionauth.jwks.JSONWebKeySetHelper;
import io.fusionauth.jwks.domain.JSONWebKey;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class JSONWebKeysCacheLoader extends BaseCacheLoader<URI, List<JSONWebKey>> implements Runnable {
  private final JSONWebKeysCache cache;
  
  private final JSONWebKeyService jsonWebKeyService;
  
  @Inject
  public JSONWebKeysCacheLoader(JSONWebKeysCache paramJSONWebKeysCache, JSONWebKeyService paramJSONWebKeyService) {
    super(Collections.emptyList());
    this.cache = paramJSONWebKeysCache;
    this.jsonWebKeyService = paramJSONWebKeyService;
  }
  
  public void run() {
    load();
  }
  
  protected void internalLoad(Cache<URI, List<JSONWebKey>> paramCache) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    List<JSONWebKeyInfoProvider> list = this.jsonWebKeyService.retrieveAllProviders();
    for (JSONWebKeyInfoProvider jSONWebKeyInfoProvider : list) {
      List<JSONWebKey> list1 = retrieveJSONWebKeys(jSONWebKeyInfoProvider.jwksURI());
      URI uRI = jSONWebKeyInfoProvider.issuer();
      if (list1 == null) {
        if (paramCache.contains(uRI))
          hashMap.put(uRI, paramCache.get(uRI)); 
        continue;
      } 
      hashMap.put(uRI, list1);
    } 
    paramCache.replace(hashMap);
  }
  
  protected Cache<URI, List<JSONWebKey>> resolveCache() {
    return (Cache<URI, List<JSONWebKey>>)this.cache;
  }
  
  private List<JSONWebKey> retrieveJSONWebKeys(URI paramURI) {
    try {
      return JSONWebKeySetHelper.retrieveKeysFromJWKS(paramURI.toString());
    } catch (io.fusionauth.jwks.JSONWebKeySetHelper.JSONWebKeySetException jSONWebKeySetException) {
      String str = "Failed to retrieve keys from JWKS [" + String.valueOf(paramURI) + "]";
      EventLogHelper.create(new EventLog(EventLogType.Error, str, (Throwable)jSONWebKeySetException));
      return null;
    } 
  }
}
