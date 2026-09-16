package io.fusionauth.api.service.lock;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.domain.DistributedLockMapper;
import io.fusionauth.api.domain.LockMapper;
import io.fusionauth.api.domain.LockType;

public class ResetDistributedLock extends AbstractDistributedLock<ResetDistributedLock> {
  @Inject
  public ResetDistributedLock(@Named("background") DistributedLockMapper paramDistributedLockMapper, @Named("background") LockMapper paramLockMapper) {
    super(600L, paramDistributedLockMapper, paramLockMapper, LockType.Reset);
  }
}
