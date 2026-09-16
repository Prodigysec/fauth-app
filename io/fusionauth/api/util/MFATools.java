package io.fusionauth.api.util;

import io.fusionauth.api.service.risk.CompositeRisk;
import io.fusionauth.domain.MultiFactorLoginPolicy;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MFATools {
  public static final String Email = "email";
  
  public static final String RecoveryCode = "recoveryCode";
  
  public static final String Sms = "sms";
  
  public static final String Totp = "totp";
  
  private static final Set<String> KNOWN_METHODS = Set.of("email", "recoveryCode", "sms", "totp");
  
  public static boolean challengeBasedOnClientRisk(@Nullable MultiFactorLoginPolicy paramMultiFactorLoginPolicy, @Nonnull CompositeRisk paramCompositeRisk) {
    // Byte code:
    //   0: aload_0
    //   1: astore_2
    //   2: iconst_0
    //   3: istore_3
    //   4: aload_2
    //   5: iload_3
    //   6: <illegal opcode> enumSwitch : (Lio/fusionauth/domain/MultiFactorLoginPolicy;I)I
    //   11: tableswitch default -> 86, -1 -> 86, 0 -> 36, 1 -> 61
    //   36: aload_1
    //   37: invokevirtual status : ()Lio/fusionauth/api/service/risk/RiskLevel;
    //   40: getfield score : D
    //   43: getstatic io/fusionauth/api/service/risk/RiskLevel.HIGH : Lio/fusionauth/api/service/risk/RiskLevel;
    //   46: getfield score : D
    //   49: dcmpl
    //   50: iflt -> 57
    //   53: iconst_1
    //   54: goto -> 87
    //   57: iconst_0
    //   58: goto -> 87
    //   61: aload_1
    //   62: invokevirtual status : ()Lio/fusionauth/api/service/risk/RiskLevel;
    //   65: getfield score : D
    //   68: getstatic io/fusionauth/api/service/risk/RiskLevel.MEDIUM : Lio/fusionauth/api/service/risk/RiskLevel;
    //   71: getfield score : D
    //   74: dcmpl
    //   75: iflt -> 82
    //   78: iconst_1
    //   79: goto -> 87
    //   82: iconst_0
    //   83: goto -> 87
    //   86: iconst_0
    //   87: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #35	-> 0
    //   #36	-> 36
    //   #37	-> 61
    //   #38	-> 86
    //   #35	-> 87
  }
  
  public static boolean isIntelligentLoginPolicy(@Nullable MultiFactorLoginPolicy paramMultiFactorLoginPolicy) {
    // Byte code:
    //   0: aload_0
    //   1: astore_1
    //   2: iconst_0
    //   3: istore_2
    //   4: aload_1
    //   5: iload_2
    //   6: <illegal opcode> enumSwitch : (Lio/fusionauth/domain/MultiFactorLoginPolicy;I)I
    //   11: tableswitch default -> 40, -1 -> 40, 0 -> 36, 1 -> 36
    //   36: iconst_1
    //   37: goto -> 41
    //   40: iconst_0
    //   41: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #49	-> 0
    //   #50	-> 36
    //   #51	-> 40
    //   #49	-> 41
  }
  
  public static boolean loginPolicyEnabled(@Nullable MultiFactorLoginPolicy paramMultiFactorLoginPolicy) {
    return (paramMultiFactorLoginPolicy != null && paramMultiFactorLoginPolicy != MultiFactorLoginPolicy.Disabled);
  }
  
  public static String normalizeTwoFactorMethodForWebhooks(String paramString) {
    if (paramString == null)
      return "unknown"; 
    if ("authenticator".equalsIgnoreCase(paramString))
      return "totp"; 
    return KNOWN_METHODS.stream()
      .filter(paramString2 -> paramString2.equalsIgnoreCase(paramString1))
      .findFirst()
      .orElse("unknown");
  }
}
