package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONBodyHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import io.fusionauth.api.domain.FusionAuthNodeMapper;
import io.fusionauth.api.domain.guice.FusionAuthInternalAPIKey;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.api.cache.ReloadRequest;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributedCacheNotifier extends Thread implements CacheNotifier, Closeable {
  private static final Logger logger = LoggerFactory.getLogger(DistributedCacheNotifier.class);
  
  private final FusionAuthNodeMapper fusionAuthNodeMapper;
  
  private final APIKey internalAPIKey;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  private final Map<List<String>, RequestCounter> reloadRequests = new ConcurrentHashMap<>();
  
  private volatile boolean shutdown;
  
  @Inject
  public DistributedCacheNotifier(FusionAuthNodeMapper paramFusionAuthNodeMapper, @FusionAuthInternalAPIKey APIKey paramAPIKey, ProxyInfoSupplier paramProxyInfoSupplier) {
    this.fusionAuthNodeMapper = paramFusionAuthNodeMapper;
    this.internalAPIKey = paramAPIKey;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
    start();
  }
  
  public void close() throws IOException {
    logger.info("Shutting down the CacheNotifier.");
    this.shutdown = true;
    interrupt();
  }
  
  public void reload(String... paramVarArgs) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    List<String> list = Arrays.asList(paramVarArgs);
    RequestCounter requestCounter = this.reloadRequests.computeIfAbsent(list, paramList -> new RequestCounter(0, paramZonedDateTime));
    requestCounter.pending++;
    long l = (requestCounter.lastSent != null) ? Duration.between(requestCounter.lastSent, zonedDateTime).toMillis() : 2147483647L;
    if (requestCounter.pending > 1 || l < 2000L) {
      long l1 = Math.abs(Duration.between(zonedDateTime, requestCounter.notBefore).toMillis());
      long l2 = Math.max(250L, Math.max(2, requestCounter.pending) * Math.max(requestCounter.lastSentAverage, 42) * 3L);
      long l3 = Math.min(15000L, l1 + l2);
      requestCounter.notBefore = zonedDateTime.plus(l3, ChronoUnit.MILLIS);
    } 
    synchronized (this) {
      notify();
    } 
  }
  
  public void reload(String paramString) {
    reload(new String[] { paramString });
  }
  
  public void run() {
    while (!this.shutdown) {
      synchronized (this) {
        try {
          wait(1423L);
        } catch (InterruptedException interruptedException) {}
      } 
      ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
      List<FusionAuthNodeMapper.FusionAuthNode> list = null;
      for (List<? extends CharSequence> list1 : this.reloadRequests.keySet()) {
        RequestCounter requestCounter = this.reloadRequests.get(list1);
        if (requestCounter.pending == 0)
          continue; 
        if (requestCounter.notBefore.isBefore(zonedDateTime)) {
          requestCounter.pending = 0;
          if (list == null) {
            list = this.fusionAuthNodeMapper.retrieveAll();
            if (list.isEmpty()) {
              logger.warn("No FusionAuth nodes have been defined. No cache reload notifications will be sent. [" + String.join(", ", list1) + "].");
              break;
            } 
          } 
          int i = 0;
          for (FusionAuthNodeMapper.FusionAuthNode fusionAuthNode : list) {
            long l = sendNotification(this.internalAPIKey, fusionAuthNode, (List)list1);
            i += (int)l;
          } 
          requestCounter.lastSentAverage = i / list.size();
          requestCounter.lastSent = ZonedDateTime.now(ZoneOffset.UTC);
        } 
      } 
    } 
  }
  
  private ClientResponse<Void, Void> callReload(APIKey paramAPIKey, FusionAuthNodeMapper.FusionAuthNode paramFusionAuthNode, List<String> paramList) {
    return (new RESTClient(void.class, void.class))
      .authorization(paramAPIKey.key)
      .url(paramFusionAuthNode.url)
      .connectTimeout(2000)
      .readTimeout(15000)
      .proxy((ProxyInfo)this.proxyInfoSupplier.get())
      .uri("/api/cache/reload")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(new ReloadRequest(paramList)))
      .post()
      .go();
  }
  
  private long sendNotification(APIKey paramAPIKey, FusionAuthNodeMapper.FusionAuthNode paramFusionAuthNode, List<String> paramList) {
    long l1 = System.currentTimeMillis();
    ClientResponse<Void, Void> clientResponse = callReload(paramAPIKey, paramFusionAuthNode, paramList);
    long l2 = System.currentTimeMillis() - l1;
    if (!clientResponse.wasSuccessful()) {
      try {
        Thread.sleep(7000L);
      } catch (InterruptedException interruptedException) {}
      l1 = System.currentTimeMillis();
      clientResponse = callReload(paramAPIKey, paramFusionAuthNode, paramList);
      l2 = System.currentTimeMillis() - l1;
      if (!clientResponse.wasSuccessful()) {
        String str1 = String.join(", ", (Iterable)paramList);
        String str2 = "Failed to request a cache reload for [" + str1 + "] on [" + paramFusionAuthNode.url + "]. Status Code [" + clientResponse.status + "]\nTook [" + l2 + "] ms";
        if (clientResponse.exception == null) {
          logger.error(str2);
        } else {
          logger.error(str2, clientResponse.exception);
        } 
      } 
    } 
    return l2;
  }
  
  private static class RequestCounter {
    public ZonedDateTime lastSent;
    
    public int lastSentAverage;
    
    public ZonedDateTime notBefore;
    
    public int pending;
    
    public RequestCounter(int param1Int, ZonedDateTime param1ZonedDateTime) {
      this.pending = param1Int;
      this.notBefore = param1ZonedDateTime;
    }
  }
}
