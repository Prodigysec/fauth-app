package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.CacheLoader;
import io.fusionauth.api.domain.CompiledLambda;
import io.fusionauth.api.service.lambda.LambdaService;
import io.fusionauth.domain.Lambda;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class LambdaCacheLoader implements CacheLoader, Runnable {
  private final LambdaCache cache;
  
  private final LambdaService lambdaService;
  
  @Inject
  public LambdaCacheLoader(LambdaCache paramLambdaCache, LambdaService paramLambdaService) {
    this.cache = paramLambdaCache;
    this.lambdaService = paramLambdaService;
  }
  
  public void load() {
    Map map = (Map)this.lambdaService.retrieveAll().stream().map(this::compile).collect(Collectors.toMap(paramCompiledLambda -> paramCompiledLambda.id, paramCompiledLambda -> paramCompiledLambda));
    this.cache.replace(map);
  }
  
  public void run() {
    load();
  }
  
  private CompiledLambda compile(Lambda paramLambda) {
    return new CompiledLambda(paramLambda);
  }
}
