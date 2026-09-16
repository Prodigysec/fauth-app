package io.fusionauth.api.domain;

import io.fusionauth.api.domain.mybatis.StringToListTypeHandler;
import java.time.ZonedDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UsageStatsMapper {
  @Insert({"INSERT INTO usage_stats ( collection_instant, instance_id, stats_version, application_version, collection_duration, sent, stats, timings, failed_stats) VALUES (#{collectionInstant}, #{instanceId}, #{statsVersion}, #{applicationVersion}, #{collectionDuration}, #{sent}, #{stats}, #{timings}, #{failedStatsAsString})"})
  void create(CollectedUsageStats paramCollectedUsageStats);
  
  @Delete({"DELETE FROM current_usage_stats"})
  void deleteAllCurrentUsageStats();
  
  @Delete({"DELETE FROM usage_stats WHERE collection_instant < #{cutoff} AND sent=TRUE"})
  int deleteOlderThan(ZonedDateTime paramZonedDateTime);
  
  @Update({"UPDATE usage_stats\nSET sent=TRUE\nWHERE collection_instant = #{collectionInstant};\n"})
  void markUsageStatSent(ZonedDateTime paramZonedDateTime);
  
  @Select({"SELECT * FROM usage_stats"})
  @Results({@Result(property = "failedStats", column = "failed_stats", javaType = List.class, typeHandler = StringToListTypeHandler.class)})
  List<CollectedUsageStats> retrieveAll();
  
  @Select({"SELECT last_modified_instant FROM current_usage_stats"})
  ZonedDateTime retrieveCurrentStatsLastModified();
  
  @Select({"SELECT stats, last_checked_instant, last_modified_instant FROM current_usage_stats"})
  @Results({@Result(property = "stats", column = "stats", javaType = List.class, typeHandler = StringToListTypeHandler.class)})
  SavedCurrentStats retrieveCurrentUsageStats();
  
  @Select({"SELECT collection_instant FROM usage_stats WHERE collection_instant < #{cutoff} ORDER BY collection_instant LIMIT 1 OFFSET #{offset}"})
  ZonedDateTime retrieveEndOffsetTime(@Param("offset") int paramInt, @Param("cutoff") ZonedDateTime paramZonedDateTime);
  
  @Select({"SELECT * FROM usage_stats WHERE sent=FALSE LIMIT #{limit}"})
  @Results({@Result(property = "failedStats", column = "failed_stats", javaType = List.class, typeHandler = StringToListTypeHandler.class)})
  List<CollectedUsageStats> retrieveUnsent(int paramInt);
  
  @Insert({"INSERT INTO current_usage_stats (stats, last_checked_instant, last_modified_instant)\nVALUES (#{statsAsString}, #{lastCheckedInstant}, #{lastModifiedInstant})\n"})
  void saveCurrentUsageStats(SavedCurrentStats paramSavedCurrentStats);
  
  @Update({"UPDATE current_usage_stats SET last_checked_instant = #{lastCheckedInstant}"})
  void updateLastCheckedCurrentStats(ZonedDateTime paramZonedDateTime);
}
