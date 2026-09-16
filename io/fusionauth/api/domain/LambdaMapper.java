package io.fusionauth.api.domain;

import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.search.LambdaSearchCriteria;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface LambdaMapper {
  @Insert({"INSERT INTO lambdas (id, body, debug, engine_type, insert_instant, last_update_instant, name, type) VALUES (#{id}, #{body}, #{debug}, #{engineType}, #{insertInstant}, #{lastUpdateInstant}, #{name}, #{type})"})
  void create(Lambda paramLambda);
  
  @Delete({"DELETE FROM lambdas WHERE id = #{id}"})
  int delete(@Param("id") UUID paramUUID);
  
  @Select({"SELECT * FROM lambdas ORDER BY insert_instant DESC"})
  List<Lambda> retrieveAll();
  
  @Select({"<script>\nSELECT * FROM lambdas\n<where>\n  <if test=\"body != null\">\n    AND LOWER(body) LIKE #{body}\n  </if>\n\n  <if test=\"name != null\">\n    AND LOWER(name) LIKE #{name}\n  </if>\n\n  <if test=\"type != null\">\n    AND type = #{type}\n  </if>\n</where>\n<if test=\"orderBy != null\">\n  ORDER BY ${orderBy}\n</if>\n<if test=\"numberOfResults != null\">\n  LIMIT #{numberOfResults}\n</if>\n<if test=\"startRow != null\">\n  OFFSET #{startRow}\n</if>\n</script>\n"})
  List<Lambda> retrieveByCriteria(LambdaSearchCriteria paramLambdaSearchCriteria);
  
  @Select({"SELECT * FROM lambdas WHERE id = #{id}"})
  Lambda retrieveById(@Param("id") UUID paramUUID);
  
  @Select({"<script>\nSELECT COUNT(*) FROM lambdas\n<where>\n  <if test=\"body != null\">\n    AND LOWER(body) LIKE #{body}\n  </if>\n\n  <if test=\"name != null\">\n    AND LOWER(name) LIKE #{name}\n  </if>\n\n  <if test=\"type != null\">\n    AND type = #{type}\n  </if>\n</where>\n</script>\n"})
  int retrieveCountByCriteria(LambdaSearchCriteria paramLambdaSearchCriteria);
  
  @Select({"SELECT * FROM lambdas WHERE type = #{type} ORDER BY name"})
  List<Lambda> retrieveEnabledByType(@Param("type") LambdaType paramLambdaType);
  
  @Update({"UPDATE lambdas SET body = #{body}, debug = #{debug}, engine_type = #{engineType}, last_update_instant = #{lastUpdateInstant}, name = #{name} WHERE id = #{id}"})
  int update(Lambda paramLambda);
}
