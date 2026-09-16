package io.fusionauth.api.service.user;

import com.inversoft.validator.Validator;

public class BotDetectionScoreValidator {
  public static void validate(Validator paramValidator, Double paramDouble, String paramString) {
    if (paramDouble == null)
      return; 
    paramValidator.ensure((paramDouble
        .doubleValue() >= 0.0D && paramDouble.doubleValue() <= 1.0D), paramString, "[invalid]", new Object[] { paramDouble });
  }
}
