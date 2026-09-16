package io.fusionauth.api.domain;

import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.search.EventLogSearchCriteria;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface EventLogMapper {
  int count(EventLogSearchCriteria paramEventLogSearchCriteria);
  
  @Insert({"INSERT INTO event_logs (insert_instant, message, type) VALUES (#{insertInstant}, #{message}, #{type})"})
  @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
  void create(EventLog paramEventLog);
  
  void deleteOld(@Param("number") int paramInt);
  
  List<EventLog> retrieveByCriteria(EventLogSearchCriteria paramEventLogSearchCriteria);
  
  @Select({"SELECT * FROM event_logs WHERE id = #{id}"})
  @ResultMap({"EventLog"})
  EventLog retrieveById(int paramInt);
}
