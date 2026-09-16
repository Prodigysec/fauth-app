package io.fusionauth.api.domain;

import org.apache.ibatis.annotations.Select;

public interface VersionMapper {
  @Select({"SELECT version FROM version"})
  String retrieveDatabaseVersion();
}
