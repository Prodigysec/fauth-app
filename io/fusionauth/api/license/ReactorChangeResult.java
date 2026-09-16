package io.fusionauth.api.license;

import com.inversoft.util.Pair;
import io.fusionauth.api.domain.ReactorHealthChecks;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class ReactorChangeResult extends Record {
  private final Set<String> addedFeatures;
  
  private final Set<String> removedFeatures;
  
  private final Map<String, Pair<ReactorFeatureStatus, ReactorFeatureStatus>> healthStatusChanges;
  
  private final boolean anyChanges;
  
  public ReactorChangeResult(Set<String> paramSet1, Set<String> paramSet2, Map<String, Pair<ReactorFeatureStatus, ReactorFeatureStatus>> paramMap, boolean paramBoolean) {
    this.addedFeatures = paramSet1;
    this.removedFeatures = paramSet2;
    this.healthStatusChanges = paramMap;
    this.anyChanges = paramBoolean;
  }
  
  public final String toString() {
    // Byte code:
    //   0: aload_0
    //   1: <illegal opcode> toString : (Lio/fusionauth/api/license/ReactorChangeResult;)Ljava/lang/String;
    //   6: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #30	-> 0
  }
  
  public final int hashCode() {
    // Byte code:
    //   0: aload_0
    //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/license/ReactorChangeResult;)I
    //   6: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #30	-> 0
  }
  
  public final boolean equals(Object paramObject) {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: <illegal opcode> equals : (Lio/fusionauth/api/license/ReactorChangeResult;Ljava/lang/Object;)Z
    //   7: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #30	-> 0
  }
  
  public Set<String> addedFeatures() {
    return this.addedFeatures;
  }
  
  public Set<String> removedFeatures() {
    return this.removedFeatures;
  }
  
  public Map<String, Pair<ReactorFeatureStatus, ReactorFeatureStatus>> healthStatusChanges() {
    return this.healthStatusChanges;
  }
  
  public boolean anyChanges() {
    return this.anyChanges;
  }
  
  public static ReactorChangeResult getChanges(ReactorHealthChecks paramReactorHealthChecks1, ReactorHealthChecks paramReactorHealthChecks2) {
    Map<String, ReactorFeatureStatus> map1 = getFeatureStatusMap(paramReactorHealthChecks1);
    Map<String, ReactorFeatureStatus> map2 = getFeatureStatusMap(paramReactorHealthChecks2);
    HashSet<String> hashSet1 = new HashSet();
    HashSet<String> hashSet2 = new HashSet();
    HashMap<Object, Object> hashMap = new HashMap<>();
    boolean[] arrayOfBoolean = { false };
    map2.forEach((paramString, paramReactorFeatureStatus) -> {
          ReactorFeatureStatus reactorFeatureStatus = (ReactorFeatureStatus)paramMap1.get(paramString);
          if (paramReactorFeatureStatus == ReactorFeatureStatus.DISABLED && reactorFeatureStatus == ReactorFeatureStatus.UNKNOWN) {
            paramArrayOfboolean[0] = true;
            return;
          } 
          if (paramReactorFeatureStatus == ReactorFeatureStatus.ACTIVE && reactorFeatureStatus == ReactorFeatureStatus.PENDING) {
            paramMap2.put(paramString, Pair.p(reactorFeatureStatus, paramReactorFeatureStatus));
          } else if (paramReactorFeatureStatus == ReactorFeatureStatus.ACTIVE && reactorFeatureStatus != ReactorFeatureStatus.ACTIVE) {
            paramSet1.add(paramString);
          } else if (paramReactorFeatureStatus == ReactorFeatureStatus.DISABLED && reactorFeatureStatus != ReactorFeatureStatus.DISABLED) {
            paramSet2.add(paramString);
          } else if (paramReactorFeatureStatus != reactorFeatureStatus) {
            paramMap2.put(paramString, Pair.p(reactorFeatureStatus, paramReactorFeatureStatus));
          } 
        });
    boolean bool = (!hashSet1.isEmpty() || !hashSet2.isEmpty() || !hashMap.isEmpty() || arrayOfBoolean[0]) ? true : false;
    return new ReactorChangeResult(hashSet1, hashSet2, (Map)hashMap, bool);
  }
  
  public static Map<String, ReactorFeatureStatus> getFeatureStatusMap(ReactorHealthChecks paramReactorHealthChecks) {
    return Map.ofEntries(new Map.Entry[] { Map.entry("breachedPasswordDetection", paramReactorHealthChecks.breachedPasswordDetection), 
          Map.entry("ipGeoLocation", paramReactorHealthChecks.ipGeoLocation), 
          Map.entry("ipReputation", paramReactorHealthChecks.ipReputation), 
          Map.entry("userAgentReputation", paramReactorHealthChecks.userAgentReputation) });
  }
  
  public Optional<String> getChangeDescription() {
    if (!anyChanges())
      return Optional.empty(); 
    ArrayList<String> arrayList = new ArrayList();
    if (!this.addedFeatures.isEmpty())
      arrayList.add("Added feature(s): %s".formatted(new Object[] { this.addedFeatures.stream().sorted().toList() })); 
    if (!this.removedFeatures.isEmpty())
      arrayList.add("Removed feature(s): %s".formatted(new Object[] { this.removedFeatures.stream().sorted().toList() })); 
    if (!this.healthStatusChanges.isEmpty()) {
      String str = this.healthStatusChanges.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(paramEntry -> "%s %s->%s".formatted(new Object[] { paramEntry.getKey(), ((Pair)paramEntry.getValue()).first, ((Pair)paramEntry.getValue()).second })).collect(Collectors.joining(", "));
      arrayList.add("Feature Health/Status Change(s): %s".formatted(new Object[] { str }));
    } 
    if (arrayList.isEmpty())
      return Optional.empty(); 
    return Optional.of(String.join(", ", (Iterable)arrayList));
  }
}
