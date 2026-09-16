package io.fusionauth.app.action.api.cache;

import com.google.inject.Inject;
import com.inversoft.cache.CacheLoader;
import com.inversoft.cache.CacheLoaderProvider;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.primeframework.UndocumentedAPI;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.cache.ReloadRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;

@UndocumentedAPI
@Action(requiresAuthentication = true, scheme = {"api-internal"})
public class ReloadAction extends BaseAPIAction {
  private final CacheLoaderProvider cacheLoaderProvider;
  
  @JSONRequest
  public ReloadRequest request;
  
  @Inject
  public ReloadAction(FrontEndSupport paramFrontEndSupport, CacheLoaderProvider paramCacheLoaderProvider) {
    super(paramFrontEndSupport);
    this.cacheLoaderProvider = paramCacheLoaderProvider;
  }
  
  public String post() {
    if (this.request == null || this.request.names == null)
      return "missing"; 
    boolean bool = false;
    for (String str : this.request.names) {
      CacheLoader cacheLoader = this.cacheLoaderProvider.get(str);
      if (cacheLoader != null) {
        cacheLoader.load();
        continue;
      } 
      bool = true;
    } 
    return bool ? "missing" : "success";
  }
}
