package io.fusionauth.api.service.user;

import io.fusionauth.domain.IdentityType;
import java.util.List;

public class IdentityTypeHelper {
  public static List<IdentityType> convert(List<String> paramList) {
    if (paramList == null)
      return List.of(); 
    return paramList.stream().map(IdentityType::of).toList();
  }
  
  public static List<String> stringify(List<IdentityType> paramList) {
    if (paramList == null)
      return List.of(); 
    return paramList.stream().map(IdentityType::toString).toList();
  }
}
