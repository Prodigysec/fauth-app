package io.fusionauth.api.service.system.task;

import com.google.inject.Inject;
import io.fusionauth.api.domain.AsyncTask;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.system.TenantService;
import io.fusionauth.domain.ObjectState;
import io.fusionauth.domain.Tenant;

public class DeleteTenantTask implements RunnableTask {
  private final TenantMapper tenantMapper;
  
  private final TenantReaderService tenantReader;
  
  private final TenantService tenantService;
  
  @Inject
  public DeleteTenantTask(TenantMapper paramTenantMapper, TenantReaderService paramTenantReaderService, TenantService paramTenantService) {
    this.tenantMapper = paramTenantMapper;
    this.tenantReader = paramTenantReaderService;
    this.tenantService = paramTenantService;
  }
  
  public Runnable get(AsyncTask paramAsyncTask) {
    Tenant tenant = this.tenantReader.retrieveById(paramAsyncTask.entityId);
    if (tenant == null || tenant.insertInstant.isAfter(paramAsyncTask.insertInstant))
      return null; 
    return () -> this.tenantService.delete(paramTenant, paramAsyncTask.eventInfo);
  }
  
  public boolean isComplete(AsyncTask paramAsyncTask) {
    return (this.tenantReader.retrieveById(paramAsyncTask.entityId) == null);
  }
  
  public void resetEntityState(AsyncTask paramAsyncTask) {
    this.tenantMapper.updateState(paramAsyncTask.entityId, ObjectState.Active);
  }
}
