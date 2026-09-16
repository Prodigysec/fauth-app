package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.TenantSearchCriteria;
import java.util.List;
import java.util.UUID;

public class DefaultTenantReaderService implements TenantReaderService {
  private final ApplicationReaderService applicationReader;
  
  private final TenantMapper tenantMapper;
  
  @Inject
  public DefaultTenantReaderService(ApplicationReaderService paramApplicationReaderService, TenantMapper paramTenantMapper) {
    this.applicationReader = paramApplicationReaderService;
    this.tenantMapper = paramTenantMapper;
  }
  
  public boolean existsById(UUID paramUUID) {
    return (this.tenantMapper.existsById(paramUUID) != null);
  }
  
  public Tenant resolve(Tenant paramTenant, Tenantable... paramVarArgs) {
    if (paramTenant != null)
      return paramTenant; 
    for (Tenantable tenantable : paramVarArgs) {
      if (tenantable != null) {
        Tenant tenant = retrieveById(tenantable.getTenantId());
        if (tenant != null)
          return tenant; 
      } 
    } 
    return null;
  }
  
  public List<Tenant> retrieveAll() {
    return this.tenantMapper.retrieveAll();
  }
  
  public List<Tenant> retrieveAllUsingEntityTypes() {
    return this.tenantMapper.retrieveAllUsingEntityTypes();
  }
  
  public List<TenantMapper.TenantId> retrieveAllUsingIPAccessControlList(UUID paramUUID) {
    return this.tenantMapper.retrieveAllIdsUsingIPAccessControlList(paramUUID);
  }
  
  public UUID retrieveApplicationTenantId(UUID paramUUID) {
    Application application = this.applicationReader.retrieveById(null, paramUUID);
    if (application == null)
      throw new NotFoundException(); 
    return application.tenantId;
  }
  
  public Tenant retrieveByApplicationId(UUID paramUUID) {
    return this.tenantMapper.retrieveTenantByApplicationId(paramUUID);
  }
  
  public Tenant retrieveById(UUID paramUUID) {
    return this.tenantMapper.retrieveById(paramUUID);
  }
  
  public int retrieveCount() {
    return this.tenantMapper.retrieveCount();
  }
  
  public int retrieveCountByThemeId(UUID paramUUID) {
    return this.tenantMapper.retrieveCountByThemeId(paramUUID);
  }
  
  public Tenant retrieveDefaultOrNull() {
    if (this.tenantMapper.retrieveCount() > 1)
      return null; 
    return this.tenantMapper.retrieveTenantByApplicationId(Application.FUSIONAUTH_APP_ID);
  }
  
  public Tenant retrieveDefaultTenant() {
    return this.tenantMapper.retrieveTenantByApplicationId(Application.FUSIONAUTH_APP_ID);
  }
  
  public UUID retrieveFusionAuthTenantId() {
    return retrieveApplicationTenantId(Application.FUSIONAUTH_APP_ID);
  }
  
  public SearchResults<Tenant> search(TenantSearchCriteria paramTenantSearchCriteria) {
    int i = this.tenantMapper.retrieveCountByCriteria(paramTenantSearchCriteria);
    List<Tenant> list = (i > 0) ? this.tenantMapper.retrieveByCriteria(paramTenantSearchCriteria) : List.of();
    return new SearchResults<>(list, i);
  }
}
