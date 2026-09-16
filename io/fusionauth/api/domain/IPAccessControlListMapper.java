package io.fusionauth.api.domain;

import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.search.IPAccessControlListSearchCriteria;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface IPAccessControlListMapper {
  public static final String IP_ACCESS_CONTROL_LIST_SELECT = "SELECT\n  ip.id                  AS ip_id,\n  ip.name                AS ip_name,\n  ip.data                AS ip_data,\n  ip.insert_instant      AS ip_insert_instant,\n  ip.last_update_instant AS ip_last_update_instant\nFROM ip_access_control_lists ip\n";
  
  @Select({"<script>\n\nSELECT COUNT(*)\nFROM ip_access_control_lists\n<where>\n  <if test=\"name != null\">\n    AND LOWER(name) LIKE #{name}\n  </if>\n</where>\n\n</script>\n"})
  @ResultType(Integer.class)
  int count(IPAccessControlListSearchCriteria paramIPAccessControlListSearchCriteria);
  
  @Insert({"  INSERT INTO ip_access_control_lists\n    (id, name, data, insert_instant, last_update_instant)\n    VALUES\n    (#{id}, #{name}, #{dataToDatabase}, #{insertInstant}, #{lastUpdateInstant})\n"})
  void create(IPAccessControlList paramIPAccessControlList);
  
  @Delete({"DELETE FROM ip_access_control_lists WHERE id = #{id}"})
  void delete(@Param("id") UUID paramUUID);
  
  @Select({"SELECT\n  ip.id                  AS ip_id,\n  ip.name                AS ip_name,\n  ip.data                AS ip_data,\n  ip.insert_instant      AS ip_insert_instant,\n  ip.last_update_instant AS ip_last_update_instant\nFROM ip_access_control_lists ip\n ORDER BY ip.insert_instant DESC"})
  @ResultMap({"IPAddressRangeMapper"})
  List<IPAccessControlList> retrieveAll();
  
  @Select({"<script>SELECT\n  ip.id                  AS ip_id,\n  ip.name                AS ip_name,\n  ip.data                AS ip_data,\n  ip.insert_instant      AS ip_insert_instant,\n  ip.last_update_instant AS ip_last_update_instant\nFROM ip_access_control_lists ip\n<where>\n  <if test=\"name != null\">\n     LOWER(ip.name) LIKE #{name}\n  </if>\n</where>\n<if test=\"orderBy != null\">\n  ORDER BY ${orderBy}\n</if>\n<if test=\"numberOfResults != null\">\n  LIMIT #{numberOfResults}\n</if>\n<if test=\"startRow != null\">\n  OFFSET #{startRow}\n</if>\n\n</script>\n"})
  @ResultMap({"IPAddressRangeMapper"})
  List<IPAccessControlList> retrieveByCriteria(IPAccessControlListSearchCriteria paramIPAccessControlListSearchCriteria);
  
  @Select({"SELECT\n  ip.id                  AS ip_id,\n  ip.name                AS ip_name,\n  ip.data                AS ip_data,\n  ip.insert_instant      AS ip_insert_instant,\n  ip.last_update_instant AS ip_last_update_instant\nFROM ip_access_control_lists ip\n WHERE id = #{id}"})
  @ResultMap({"IPAddressRangeMapper"})
  IPAccessControlList retrieveById(@Param("id") UUID paramUUID);
  
  @Select({"<script>SELECT\n  ip.id                  AS ip_id,\n  ip.name                AS ip_name,\n  ip.data                AS ip_data,\n  ip.insert_instant      AS ip_insert_instant,\n  ip.last_update_instant AS ip_last_update_instant\nFROM ip_access_control_lists ip\nWHERE\n  <if test=\"id != null\">\n    ip.id != #{id} AND\n  </if>\n  name = #{name}\n</script>\n"})
  @ResultMap({"IPAddressRangeMapper"})
  IPAccessControlList retrieveExisting(@Param("id") UUID paramUUID, @Param("name") String paramString);
  
  @Update({"UPDATE ip_access_control_lists\n  SET\n    name = #{name},\n    data = #{dataToDatabase},\n    last_update_instant = #{lastUpdateInstant}\nWHERE id = #{id}\n"})
  void update(IPAccessControlList paramIPAccessControlList);
}
