package io.fusionauth.api.service.system;

import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.TenantSearchCriteria;
import java.util.List;
import java.util.UUID;

public interface TenantReaderService {
  boolean existsById(UUID paramUUID);
  
  Tenant resolve(Tenant paramTenant, Tenantable... paramVarArgs);
  
  List<Tenant> retrieveAll();
  
  List<Tenant> retrieveAllUsingEntityTypes();
  
  List<TenantMapper.TenantId> retrieveAllUsingIPAccessControlList(UUID paramUUID);
  
  UUID retrieveApplicationTenantId(UUID paramUUID);
  
  Tenant retrieveByApplicationId(UUID paramUUID);
  
  Tenant retrieveById(UUID paramUUID);
  
  int retrieveCount();
  
  int retrieveCountByThemeId(UUID paramUUID);
  
  Tenant retrieveDefaultOrNull();
  
  Tenant retrieveDefaultTenant();
  
  UUID retrieveFusionAuthTenantId();
  
  SearchResults<Tenant> search(TenantSearchCriteria paramTenantSearchCriteria);
}
