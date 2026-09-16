package io.fusionauth.api.service.lock;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.domain.Lock;
import io.fusionauth.api.domain.LockMapper;
import io.fusionauth.api.domain.LockType;

public class DefaultLockService implements LockService {
  private final LockMapper backgroundLockMapper;
  
  @Inject
  public DefaultLockService(@Named("background") LockMapper paramLockMapper) {
    this.backgroundLockMapper = paramLockMapper;
  }
  
  public void acquireLock(LockType paramLockType) {
    Lock lock = this.backgroundLockMapper.lock(paramLockType);
    if (lock == null)
      throw new IllegalStateException("Invalid lock [" + String.valueOf(paramLockType) + "]. Your database is missing lock rows that are required for FusionAuth to work properly."); 
  }
}
