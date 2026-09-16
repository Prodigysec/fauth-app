package io.fusionauth.api.service.risk;

import io.fusionauth.domain.AuthenticationThreats;

public final class RiskSignal extends Record {
  private final AuthenticationThreats type;
  
  private final RiskLevel level;
  
  public RiskSignal(AuthenticationThreats paramAuthenticationThreats, RiskLevel paramRiskLevel) {
    this.type = paramAuthenticationThreats;
    this.level = paramRiskLevel;
  }
  
  public final int hashCode() {
    // Byte code:
    //   0: aload_0
    //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/risk/RiskSignal;)I
    //   6: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #11	-> 0
  }
  
  public final boolean equals(Object paramObject) {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/risk/RiskSignal;Ljava/lang/Object;)Z
    //   7: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #11	-> 0
  }
  
  public AuthenticationThreats type() {
    return this.type;
  }
  
  public RiskLevel level() {
    return this.level;
  }
  
  public String toString() {
    return String.valueOf(this.type) + "=" + String.valueOf(this.type);
  }
}
