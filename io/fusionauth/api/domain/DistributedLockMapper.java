package io.fusionauth.api.domain;

import java.time.ZonedDateTime;
import org.apache.ibatis.annotations.Param;

public interface DistributedLockMapper {
  void checkin(@Param("type") LockType paramLockType, @Param("now") ZonedDateTime paramZonedDateTime1, @Param("expireTime") ZonedDateTime paramZonedDateTime2);
  
  boolean isLocked(@Param("type") LockType paramLockType, @Param("expireTime") ZonedDateTime paramZonedDateTime);
  
  void lock(@Param("type") LockType paramLockType, @Param("now") ZonedDateTime paramZonedDateTime1, @Param("expireTime") ZonedDateTime paramZonedDateTime2);
  
  void unlock(@Param("type") LockType paramLockType);
}
