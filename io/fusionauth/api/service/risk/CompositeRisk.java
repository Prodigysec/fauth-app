package io.fusionauth.api.service.risk;

import io.fusionauth.domain.AuthenticationThreats;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public final class CompositeRisk extends Record {
  @Nonnull
  private final RiskLevel status;
  
  @Nonnull
  private final Map<AuthenticationThreats, RiskLevel> signals;
  
  private final boolean isSuspicious;
  
  public final int hashCode() {
    // Byte code:
    //   0: aload_0
    //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/risk/CompositeRisk;)I
    //   6: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #21	-> 0
  }
  
  public final boolean equals(Object paramObject) {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/risk/CompositeRisk;Ljava/lang/Object;)Z
    //   7: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #21	-> 0
  }
  
  @Nonnull
  public RiskLevel status() {
    return this.status;
  }
  
  @Nonnull
  public Map<AuthenticationThreats, RiskLevel> signals() {
    return this.signals;
  }
  
  public boolean isSuspicious() {
    return this.isSuspicious;
  }
  
  public static final CompositeRisk NO_RISK = new CompositeRisk(RiskLevel.LOW, Map.of(), false);
  
  public CompositeRisk(RiskLevel paramRiskLevel, Map<AuthenticationThreats, RiskLevel> paramMap, boolean paramBoolean) {
    this.status = paramRiskLevel;
    this.signals = Collections.unmodifiableMap(paramMap);
    this.isSuspicious = paramBoolean;
  }
  
  public static CompositeRisk compute(List<RiskSignal> paramList) {
    RiskLevel riskLevel;
    boolean bool = false;
    if (paramList == null || paramList.isEmpty())
      return new CompositeRisk(RiskLevel.NOT_AVAILABLE, new EnumMap<>(AuthenticationThreats.class), bool); 
    EnumMap<AuthenticationThreats, Object> enumMap = new EnumMap<>(AuthenticationThreats.class);
    double d1 = 0.0D;
    for (RiskSignal riskSignal : paramList) {
      if (enumMap.containsKey(riskSignal.type()))
        continue; 
      enumMap.put(riskSignal.type(), riskSignal.level());
      d1 += (riskSignal.level()).score;
      if (riskSignal.level() == RiskLevel.HIGH)
        bool = true; 
    } 
    double d2 = d1 / enumMap.size();
    if (d2 < 0.25D) {
      riskLevel = RiskLevel.LOW;
    } else if (d2 <= 0.5D) {
      riskLevel = RiskLevel.MEDIUM;
    } else {
      riskLevel = RiskLevel.HIGH;
    } 
    if (riskLevel == RiskLevel.LOW && bool)
      riskLevel = RiskLevel.MEDIUM; 
    return new CompositeRisk(riskLevel, (Map)enumMap, bool);
  }
  
  public Set<AuthenticationThreats> getHighRiskAuthenticationThreats() {
    return (Set<AuthenticationThreats>)this.signals.entrySet().stream()
      .filter(paramEntry -> (paramEntry.getValue() == RiskLevel.HIGH))
      .map(Map.Entry::getKey)
      .collect(Collectors.toCollection(() -> EnumSet.noneOf(AuthenticationThreats.class)));
  }
  
  public Set<AuthenticationThreats> getThreatsDetected() {
    return (this.signals.get(AuthenticationThreats.ImpossibleTravel) == RiskLevel.HIGH) ? Set.of(AuthenticationThreats.ImpossibleTravel) : Set.of();
  }
  
  public String toString() {
    return "CompositeRisk{status=" + String.valueOf(this.status) + ", signals=" + String.valueOf(this.signals) + "}";
  }
}
