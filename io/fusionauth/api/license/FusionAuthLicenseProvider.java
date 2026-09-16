package io.fusionauth.api.license;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.inversoft.license.v2.DefaultLicenseProvider;
import com.inversoft.license.v2.domain.LicenseContainer;
import com.inversoft.license.v2.domain.LicenseFeature;
import com.inversoft.license.v2.domain.LicenseFeatureType;
import com.inversoft.license.v2.guice.LicenseLocalReload;
import com.inversoft.license.v2.guice.LicenseNetworkReload;
import com.inversoft.license.v2.local.LocalLicenseSystem;
import com.inversoft.license.v2.network.NetworkLicenseSystem;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.api.service.system.NodeService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusionAuthLicenseProvider extends DefaultLicenseProvider {
  private static final Logger logger = LoggerFactory.getLogger(FusionAuthLicenseProvider.class);
  
  private final Injector injector;
  
  private final Provider<NodeService> nodeServiceProvider;
  
  @Inject
  public FusionAuthLicenseProvider(LocalLicenseSystem paramLocalLicenseSystem, NetworkLicenseSystem paramNetworkLicenseSystem, @LicenseNetworkReload int paramInt1, @LicenseLocalReload int paramInt2, Injector paramInjector, Provider<NodeService> paramProvider) {
    super(paramLocalLicenseSystem, paramNetworkLicenseSystem, paramInt1, paramInt2);
    logger.info("Initialized");
    this.injector = paramInjector;
    this.nodeServiceProvider = paramProvider;
  }
  
  public void scheduledLicenseReloadComplete(LicenseContainer paramLicenseContainer) {
    logger.debug("License reloaded from network");
    LicenseContainer licenseContainer = getLicense();
    if (licenseContainer == null && paramLicenseContainer == null)
      return; 
    ReactorService reactorService = (ReactorService)this.injector.getInstance(ReactorService.class);
    if (licenseContainer == null) {
      logger.info("License was revoked");
      reactorService.updateStatusWithHealthCheck();
      return;
    } 
    if (paramLicenseContainer == null) {
      logger.info("License change triggering a health check update and activation process");
      reactorService.updateStatusWithNewFeatures();
      return;
    } 
    LicenseChangeResult licenseChangeResult = LicenseChangeResult.getLicenseChanges(paramLicenseContainer, licenseContainer);
    if (!licenseChangeResult.anyChanges())
      return; 
    logger.info("License features have changed. {}", licenseChangeResult
        .getChangeDescription().orElse(""));
    if (!licenseChangeResult.addedFeatures.isEmpty() || !licenseChangeResult.updatedCapabilities.isEmpty()) {
      logger.info("License change triggering a health check update and activation process");
      reactorService.updateStatusWithNewFeatures();
    } else {
      logger.info("License change triggering a health check update");
      reactorService.updateStatusWithHealthCheck();
    } 
  }
  
  public boolean shouldScheduledNetworkReload() {
    NodeService nodeService = (NodeService)this.nodeServiceProvider.get();
    return nodeService.isMaster();
  }
  
  static final class LicenseChangeResult extends Record {
    private final Set<LicenseFeatureType> removedFeatures;
    
    private final Set<LicenseFeatureType> addedFeatures;
    
    private final Set<LicenseFeatureType> updatedCapabilities;
    
    LicenseChangeResult(Set<LicenseFeatureType> param1Set1, Set<LicenseFeatureType> param1Set2, Set<LicenseFeatureType> param1Set3) {
      this.removedFeatures = param1Set1;
      this.addedFeatures = param1Set2;
      this.updatedCapabilities = param1Set3;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/license/FusionAuthLicenseProvider$LicenseChangeResult;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #122	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/license/FusionAuthLicenseProvider$LicenseChangeResult;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #122	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/license/FusionAuthLicenseProvider$LicenseChangeResult;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #122	-> 0
    }
    
    public Set<LicenseFeatureType> removedFeatures() {
      return this.removedFeatures;
    }
    
    public Set<LicenseFeatureType> addedFeatures() {
      return this.addedFeatures;
    }
    
    public Set<LicenseFeatureType> updatedCapabilities() {
      return this.updatedCapabilities;
    }
    
    static LicenseChangeResult getLicenseChanges(LicenseContainer param1LicenseContainer1, LicenseContainer param1LicenseContainer2) {
      Map map1 = (param1LicenseContainer1.license()).features;
      Set<?> set1 = map1.keySet();
      Map map2 = (param1LicenseContainer2.license()).features;
      Set<?> set2 = map2.keySet();
      HashSet<LicenseFeatureType> hashSet1 = new HashSet(set1);
      hashSet1.removeAll(set2);
      HashSet<LicenseFeatureType> hashSet2 = new HashSet(set2);
      hashSet2.removeAll(set1);
      Set<LicenseFeatureType> set = (Set)map2.entrySet().stream().filter(param1Entry -> {
            LicenseFeature licenseFeature = (LicenseFeature)param1Map.get(param1Entry.getKey());
            return (licenseFeature != null && !licenseFeature.equals(param1Entry.getValue()));
          }).map(Map.Entry::getKey).collect(Collectors.toSet());
      return new LicenseChangeResult(hashSet1, hashSet2, set);
    }
    
    public boolean anyChanges() {
      return (!this.removedFeatures.isEmpty() || !this.addedFeatures.isEmpty() || !this.updatedCapabilities.isEmpty());
    }
    
    public Optional<String> getChangeDescription() {
      if (!anyChanges())
        return Optional.empty(); 
      ArrayList<String> arrayList = new ArrayList();
      if (!this.addedFeatures.isEmpty())
        arrayList.add("Added feature(s): %s".formatted(new Object[] { this.addedFeatures })); 
      if (!this.removedFeatures.isEmpty())
        arrayList.add("Removed feature(s): %s".formatted(new Object[] { this.removedFeatures })); 
      if (!this.updatedCapabilities.isEmpty())
        arrayList.add("Updated capabilities on feature(s): %s".formatted(new Object[] { this.updatedCapabilities })); 
      return Optional.of(String.join(", ", (Iterable)arrayList));
    }
  }
}
