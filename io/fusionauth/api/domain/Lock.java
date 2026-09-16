package io.fusionauth.api.domain;

public class Lock {
  public LockType type;
  
  public Lock() {}
  
  public Lock(LockType paramLockType) {
    this.type = paramLockType;
  }
}
