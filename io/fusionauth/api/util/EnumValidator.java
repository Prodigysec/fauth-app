package io.fusionauth.api.util;

import com.inversoft.validator.Validator;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumValidator {
  public static void validate(Validator paramValidator, String paramString1, Enum<? extends Enum<?>>[] paramArrayOfEnum, String paramString2) {
    String str = Arrays.<Enum<? extends Enum<?>>>stream(paramArrayOfEnum).map(Enum::name).collect(Collectors.joining(", "));
    paramValidator.validEnumValue(paramString1, (Enum[])paramArrayOfEnum, paramString2, new Object[] { paramString1, str });
  }
}
