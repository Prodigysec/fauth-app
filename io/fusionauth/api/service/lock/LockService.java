package io.fusionauth.api.service.lock;

import io.fusionauth.api.domain.LockType;

public interface LockService {
  void acquireLock(LockType paramLockType);
}
