package io.fusionauth.api.domain;

import com.inversoft.json.JacksonConstructor;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface MasterRecordMapper {
  @Update({"UPDATE master_record SET id = #{id}, instant = #{instant}"})
  void claim(MasterRecord paramMasterRecord);
  
  @Update({"UPDATE master_record SET instant = 0"})
  void release();
  
  @Select({"SELECT id, instant FROM master_record FOR UPDATE "})
  MasterRecord retrieveAndLock();
  
  @Update({"UPDATE master_record SET instant = #{instant}"})
  void update(@Param("instant") ZonedDateTime paramZonedDateTime);
  
  public static class MasterRecord {
    public UUID id;
    
    public ZonedDateTime instant;
    
    @JacksonConstructor
    public MasterRecord() {}
    
    public MasterRecord(UUID param1UUID, ZonedDateTime param1ZonedDateTime) {
      this.id = param1UUID;
      this.instant = param1ZonedDateTime;
    }
  }
}
