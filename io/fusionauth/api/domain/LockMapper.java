package io.fusionauth.api.domain;

import org.apache.ibatis.annotations.Select;

public interface LockMapper {
  @Select({"SELECT type FROM locks WHERE type = #{type} FOR UPDATE"})
  Lock lock(LockType paramLockType);
}
