package io.fusionauth.api.service.lock;

import io.fusionauth.api.domain.DistributedLockMapper;
import io.fusionauth.api.domain.LockMapper;
import io.fusionauth.api.domain.LockType;
import io.fusionauth.api.domain.guice.mybatis.UseDataSource;
import java.io.Closeable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.mybatis.guice.transactional.Transactional;

public abstract class AbstractDistributedLock<T extends AbstractDistributedLock<T>> implements Closeable {
  private final DistributedLockMapper backgroundDistributedLockMapper;
  
  private final LockMapper backgroundLockMapper;
  
  private final long defaultLockTimeout;
  
  private final LockType type;
  
  protected AbstractDistributedLock(long paramLong, DistributedLockMapper paramDistributedLockMapper, LockMapper paramLockMapper, LockType paramLockType) {
    this.defaultLockTimeout = paramLong;
    this.backgroundDistributedLockMapper = paramDistributedLockMapper;
    this.backgroundLockMapper = paramLockMapper;
    this.type = paramLockType;
  }
  
  public void close() {
    unlock();
  }
  
  @Transactional
  @UseDataSource("background")
  public boolean isLocked() {
    this.backgroundLockMapper.lock(this.type);
    return this.backgroundDistributedLockMapper.isLocked(this.type, ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(getLockTimeout()));
  }
  
  public void keepAlive() {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    this.backgroundDistributedLockMapper.checkin(this.type, zonedDateTime, zonedDateTime.minusSeconds(getLockTimeout()));
  }
  
  @Transactional
  @UseDataSource("background")
  public T lock() {
    this.backgroundLockMapper.lock(this.type);
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    long l = getLockTimeout();
    if (this.backgroundDistributedLockMapper.isLocked(this.type, zonedDateTime.minusSeconds(l)))
      return null; 
    this.backgroundDistributedLockMapper.lock(this.type, zonedDateTime, zonedDateTime.minusSeconds(l));
    return (T)this;
  }
  
  public void unlock() {
    this.backgroundDistributedLockMapper.unlock(this.type);
  }
  
  protected long getLockTimeout() {
    return this.defaultLockTimeout;
  }
}
