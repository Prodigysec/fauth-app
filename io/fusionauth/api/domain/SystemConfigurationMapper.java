package io.fusionauth.api.domain;

import io.fusionauth.domain.SystemConfiguration;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface SystemConfigurationMapper {
  @Select({"SELECT * FROM system_configuration"})
  @ResultMap({"SystemConfiguration"})
  SystemConfiguration retrieve();
  
  @Select({"SELECT * FROM system_configuration FOR UPDATE "})
  @ResultMap({"SystemConfiguration"})
  SystemConfiguration retrieveAndLock();
  
  @Update({"UPDATE system_configuration SET data = #{dataToDatabase}, report_timezone = #{reportTimezone}"})
  void update(SystemConfiguration paramSystemConfiguration);
}
