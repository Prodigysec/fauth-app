package io.fusionauth.api.domain;

import io.fusionauth.domain.Integrations;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.session.ResultHandler;

public interface IntegrationMapper {
  @Select({"SELECT * FROM integrations"})
  @ResultType(Integrations.class)
  void retrieve(ResultHandler<?> paramResultHandler);
  
  @Update({"UPDATE integrations SET data = #{integrations}"})
  void update(@Param("integrations") Integrations paramIntegrations);
}
