package io.fusionauth.api.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RegistrationCountMapper {
  void bulkUpsertApplication(@Param("counts") Collection<IntervalCount> paramCollection);
  
  void bulkUpsertGlobal(@Param("counts") Collection<IntervalCount> paramCollection);
  
  @Delete({"DELETE FROM application_registration_counts WHERE applications_id = #{applicationId}"})
  void deleteCountsForApplication(UUID paramUUID);
  
  @Delete({"DELETE FROM global_registration_counts"})
  void deleteCountsForGlobal();
  
  @Delete({"DELETE FROM raw_application_registration_counts WHERE applications_id NOT IN (SELECT id from applications)"})
  void deleteOrphanedRawApplication();
  
  @Delete({"DELETE FROM raw_application_registration_counts WHERE id <= #{lastId}"})
  void deleteRawApplicationWhereIdLessThanOrEqual(long paramLong);
  
  @Delete({"DELETE FROM raw_application_registration_counts WHERE applications_id = #{applicationId}"})
  void deleteRawCountsForApplication(UUID paramUUID);
  
  @Delete({"DELETE FROM raw_global_registration_counts WHERE id <= #{lastId}"})
  void deleteRawGlobalWhereIdLessThanOrEqual(long paramLong);
  
  @Insert({"INSERT INTO raw_application_registration_counts (applications_id, count, decremented_count, hour) VALUES\n  (#{count.applicationId}, #{count.count}, #{count.decrementedCount}, #{count.period})\n"})
  void insertRawApplication(@Param("count") IntervalCount paramIntervalCount);
  
  @Insert({"INSERT INTO raw_global_registration_counts (count, decremented_count, hour) VALUES\n  (#{count.count}, #{count.decrementedCount}, #{count.period})\n"})
  void insertRawGlobal(@Param("count") IntervalCount paramIntervalCount);
  
  @Select({"SELECT applications_id AS applicationId, count AS count, decremented_count AS decrementedCount, hour AS period FROM application_registration_counts WHERE applications_id = #{applicationId} AND hour >= #{startHour} AND hour < #{endHour}"})
  List<IntervalCount> retrieveApplicationBetween(@Param("applicationId") UUID paramUUID, @Param("startHour") int paramInt1, @Param("endHour") int paramInt2);
  
  @Select({"SELECT COALESCE(SUM(count), 0) -  COALESCE(SUM(decremented_count), 0) FROM application_registration_counts WHERE applications_id = #{applicationId}"})
  long retrieveApplicationCurrentTotal(UUID paramUUID);
  
  @Select({"SELECT COALESCE(SUM(decremented_count), 0) FROM application_registration_counts WHERE applications_id = #{applicationId}"})
  long retrieveApplicationDecrementedTotal(UUID paramUUID);
  
  @Select({"SELECT COALESCE(SUM(count), 0) FROM application_registration_counts WHERE applications_id = #{applicationId}"})
  long retrieveApplicationTotal(UUID paramUUID);
  
  @Select({"SELECT count AS count, decremented_count AS decrementedCount, hour AS period FROM global_registration_counts WHERE hour >= #{startHour} AND hour < #{endHour}"})
  List<IntervalCount> retrieveGlobalBetween(@Param("startHour") int paramInt1, @Param("endHour") int paramInt2);
  
  @Select({"SELECT COALESCE(SUM(count), 0) - COALESCE(SUM(decremented_count), 0) FROM global_registration_counts"})
  long retrieveGlobalCurrentTotal();
  
  @Select({"SELECT COALESCE(SUM(decremented_count), 0) FROM global_registration_counts"})
  long retrieveGlobalDecrementedTotal();
  
  @Select({"SELECT COALESCE(SUM(count), 0) FROM global_registration_counts"})
  long retrieveGlobalTotal();
  
  @Select({"SELECT\nMAX(id) AS id,\napplications_id AS applicationId,\nSUM(count) AS count,\nSUM(decremented_count) AS decrementedCount,\nhour AS period\nFROM raw_application_registration_counts\nGROUP BY applications_id, hour\n"})
  List<IntervalCount> retrieveRawApplication();
  
  @Select({"SELECT\nMAX(id) AS id,\nSUM(count) AS count,\nSUM(decremented_count) AS decrementedCount,\nhour AS period\nFROM raw_global_registration_counts\nGROUP BY hour\n"})
  List<IntervalCount> retrieveRawGlobal();
}
