package io.fusionauth.api.service.ip;

import com.inversoft.error.Errors;
import io.fusionauth.domain.IPAccessControlList;
import java.util.UUID;

public interface IPAccessControlListService {
  void create(IPAccessControlList paramIPAccessControlList);
  
  void delete(IPAccessControlList paramIPAccessControlList);
  
  void update(IPAccessControlList paramIPAccessControlList1, IPAccessControlList paramIPAccessControlList2);
  
  ValidationResult validateCreate(IPAccessControlList paramIPAccessControlList);
  
  ValidationResult validateDelete(UUID paramUUID);
  
  ValidationResult validateUpdate(IPAccessControlList paramIPAccessControlList);
  
  public static class ValidationResult {
    public Errors errors;
    
    public IPAccessControlList existing;
  }
}
