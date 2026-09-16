package io.fusionauth.api.service.system;

import com.inversoft.error.Errors;
import io.fusionauth.domain.Key;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public interface KeyService {
  public static final UUID HS256_CLIENT_SECRET_SHADOW = UUID.fromString("092dbedc-30af-4149-9c61-b578f2c72f59");
  
  public static final UUID HS384_CLIENT_SECRET_SHADOW = UUID.fromString("4b8f1c06-518e-45bd-9ac5-d549686ae02a");
  
  public static final UUID HS512_CLIENT_SECRET_SHADOW = UUID.fromString("c753a44d-7f2e-48d3-bc4e-c2c16488a23b");
  
  public static final UUID TENANT_MANAGER_HS256_CLIENT_SECRET_SHADOW = UUID.fromString("80ccfe1e-209e-1725-bb37-6c4214dea375");
  
  public static final Set<UUID> ClientSecretShadowKeys = new HashSet<>(Arrays.asList(new UUID[] { HS256_CLIENT_SECRET_SHADOW, HS384_CLIENT_SECRET_SHADOW, HS512_CLIENT_SECRET_SHADOW, TENANT_MANAGER_HS256_CLIENT_SECRET_SHADOW }));
  
  void create(Key paramKey);
  
  void delete(UUID paramUUID);
  
  Key resetDefaultKey();
  
  void update(Key paramKey1, Key paramKey2);
  
  ValidationResult validate(Key paramKey, boolean paramBoolean);
  
  Errors validateDelete(UUID paramUUID);
  
  Errors validateImport(Key paramKey);
  
  public static class ValidationResult {
    public Errors errors;
    
    public Key existing;
    
    public Key key;
  }
}
