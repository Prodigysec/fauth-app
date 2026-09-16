package io.fusionauth.api.service.user;

import com.inversoft.validator.Validator;
import io.fusionauth.domain.IdentityType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IdentityTypeValidator {
  public static final Set<IdentityType> AllIdentityTypes = new HashSet<>(Arrays.asList(new IdentityType[] { IdentityType.email, IdentityType.phoneNumber, IdentityType.username }));
  
  public static final Set<IdentityType> VerifiableIdentityTypes = new HashSet<>(Arrays.asList(new IdentityType[] { IdentityType.email, IdentityType.phoneNumber }));
  
  public static void validate(Validator paramValidator, String paramString1, String paramString2) {
    validate(paramValidator, paramString1, paramString2, AllIdentityTypes);
  }
  
  public static void validate(Validator paramValidator, String paramString1, String paramString2, Set<IdentityType> paramSet) {
    if (paramString1 == null)
      return; 
    paramValidator.ensure(paramSet
        .stream().anyMatch(paramIdentityType -> paramIdentityType.is(paramString)), paramString2, "[invalid]", new Object[] { paramString1, 
          
          String.join(", ", paramSet.stream().map(IdentityType::toString).sorted().toList()) });
  }
  
  public static void validate(Validator paramValidator, List<String> paramList, String paramString) {
    if (paramList == null)
      return; 
    paramValidator.forEach(paramList, (paramValidator, paramString2, paramInteger) -> paramValidator.ensureWithCode(AllIdentityTypes.stream().anyMatch(()), paramString1 + "[" + paramString1 + "]", "[invalid]" + paramString1, new Object[] { paramString2, String.join(", ", AllIdentityTypes.stream().map(IdentityType::toString).sorted().toList()) }));
  }
}
