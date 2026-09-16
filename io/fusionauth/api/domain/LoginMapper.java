package io.fusionauth.api.domain;

import io.fusionauth.api.service.authentication.LoginQueue;
import io.fusionauth.domain.DisplayableRawLogin;
import io.fusionauth.domain.RawLogin;
import io.fusionauth.domain.search.LoginRecordSearchCriteria;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface LoginMapper {
  public static final String SELECT_RAW_LOGINS = "SELECT applications_id, instant, ip_address, users_id, identities_value, identities_type FROM raw_logins %s ORDER BY instant DESC";
  
  void createRawLogins(@Param("rawLogins") List<LoginQueue.LoginQueueRawLogin> paramList);
  
  @Delete({"DELETE FROM application_daily_active_users WHERE applications_id = #{id}"})
  void deleteDailyActiveForApplication(UUID paramUUID);
  
  @Delete({"DELETE FROM global_daily_active_users"})
  void deleteDailyActiveForGlobal();
  
  @Delete({"DELETE FROM hourly_logins WHERE applications_id = #{id}"})
  void deleteHourlyLoginsForApplication(UUID paramUUID);
  
  @Delete({"DELETE FROM application_monthly_active_users WHERE applications_id = #{id}"})
  void deleteMonthlyActiveForApplication(UUID paramUUID);
  
  @Delete({"DELETE FROM global_monthly_active_users"})
  void deleteMonthlyActiveForGlobal();
  
  @Delete({"DELETE FROM raw_logins WHERE instant < #{cutoff}"})
  int deleteOlderThan(ZonedDateTime paramZonedDateTime);
  
  @Delete({"DELETE FROM raw_application_daily_active_users WHERE applications_id = #{applicationId} AND day = #{period}"})
  void deleteRawApplicationDailyActives(IntervalCount paramIntervalCount);
  
  @Delete({"DELETE FROM raw_application_monthly_active_users WHERE applications_id = #{applicationId} AND month = #{period}"})
  void deleteRawApplicationMonthlyActives(IntervalCount paramIntervalCount);
  
  @Delete({"DELETE FROM raw_application_daily_active_users WHERE applications_id = #{id}"})
  void deleteRawDailyActiveForApplication(UUID paramUUID);
  
  @Delete({"DELETE FROM raw_global_daily_active_users WHERE day = #{period}"})
  void deleteRawGlobalDailyActives(IntervalCount paramIntervalCount);
  
  @Delete({"DELETE FROM raw_global_monthly_active_users WHERE month = #{period}"})
  void deleteRawGlobalMonthlyActives(IntervalCount paramIntervalCount);
  
  @Delete({"DELETE FROM raw_logins WHERE applications_id = #{id}"})
  void deleteRawLoginsForApplication(UUID paramUUID);
  
  @Delete({"DELETE FROM raw_logins WHERE users_id = #{id}"})
  void deleteRawLoginsForUser(UUID paramUUID);
  
  @Delete({"DELETE FROM raw_application_monthly_active_users WHERE applications_id = #{id}"})
  void deleteRawMonthlyActiveForApplication(UUID paramUUID);
  
  void replaceApplicationDailyActives(@Param("counts") List<IntervalCount> paramList);
  
  void replaceApplicationMonthlyActives(@Param("counts") List<IntervalCount> paramList);
  
  void replaceGlobalDailyActives(@Param("counts") List<IntervalCount> paramList);
  
  void replaceGlobalMonthlyActives(@Param("counts") List<IntervalCount> paramList);
  
  @Select({"SELECT applications_id AS applicationId, instant AS instant, ip_address AS ipAddress, users_id AS userId FROM raw_logins"})
  List<RawLogin> retrieveAll();
  
  @Select({"SELECT COALESCE(SUM(count), 0) FROM hourly_logins WHERE applications_id = #{applicationId}"})
  long retrieveApplicationTotal(UUID paramUUID);
  
  @Select({"SELECT applications_id AS applicationId, count AS count, day AS period FROM application_daily_active_users WHERE applications_id = #{applicationId} AND day = #{day}"})
  IntervalCount retrieveDailyActive(@Param("day") int paramInt, @Param("applicationId") UUID paramUUID);
  
  @Select({"SELECT applications_id AS applicationId, count AS count, day AS period FROM application_daily_active_users WHERE applications_id = #{applicationId} AND day >= #{start} AND day < #{end}"})
  List<IntervalCount> retrieveDailyActives(@Param("applicationId") UUID paramUUID, @Param("start") int paramInt1, @Param("end") int paramInt2);
  
  @Select({"SELECT instant FROM raw_logins WHERE instant < #{cutoff} order by instant limit 1 offset #{offset}"})
  ZonedDateTime retrieveEndOffsetTime(@Param("offset") int paramInt, @Param("cutoff") ZonedDateTime paramZonedDateTime);
  
  @Select({"SELECT count AS count, day AS period FROM global_daily_active_users WHERE day = #{day}"})
  IntervalCount retrieveGlobalDailyActive(@Param("day") int paramInt);
  
  @Select({"SELECT count AS count, day AS period FROM global_daily_active_users WHERE day >= #{start} AND day < #{end}"})
  List<IntervalCount> retrieveGlobalDailyActives(@Param("start") int paramInt1, @Param("end") int paramInt2);
  
  @Select({"SELECT SUM(count) AS count, hour AS period FROM hourly_logins WHERE hour >= #{start} AND hour < #{end} GROUP BY hour"})
  List<IntervalCount> retrieveGlobalHourlyLogins(@Param("start") int paramInt1, @Param("end") int paramInt2);
  
  @Select({"SELECT COALESCE(SUM(count), 0) AS count FROM hourly_logins WHERE hour >= #{start} AND hour < #{end}"})
  long retrieveGlobalHourlyLoginsTotal(@Param("start") int paramInt1, @Param("end") int paramInt2);
  
  @Select({"SELECT count AS count, month AS period FROM global_monthly_active_users WHERE month = #{month}"})
  IntervalCount retrieveGlobalMonthlyActive(@Param("month") int paramInt);
  
  @Select({"SELECT count AS count, month AS period FROM global_monthly_active_users WHERE month >= #{start} AND month < #{end}"})
  List<IntervalCount> retrieveGlobalMonthlyActives(@Param("start") int paramInt1, @Param("end") int paramInt2);
  
  @Select({"SELECT applications_id AS applicationId, count AS count, hour AS period FROM hourly_logins WHERE applications_id = #{applicationId} AND hour = #{hour}"})
  IntervalCount retrieveHourly(@Param("hour") int paramInt, @Param("applicationId") UUID paramUUID);
  
  @Select({"SELECT applications_id AS applicationId, count AS count, hour AS period FROM hourly_logins WHERE applications_id = #{applicationId} AND hour >= #{start} AND hour < #{end} ORDER BY hour"})
  List<IntervalCount> retrieveHourlyLogins(@Param("applicationId") UUID paramUUID, @Param("start") int paramInt1, @Param("end") int paramInt2);
  
  @Select({"SELECT applications_id AS applicationId, count AS count, month AS period FROM application_monthly_active_users WHERE applications_id = #{applicationId} AND month = #{month}"})
  IntervalCount retrieveMonthlyActive(@Param("month") int paramInt, @Param("applicationId") UUID paramUUID);
  
  @Select({"SELECT applications_id AS applicationId, count AS count, month AS period FROM application_monthly_active_users WHERE applications_id = #{applicationId} AND month >= #{start} AND month < #{end}"})
  List<IntervalCount> retrieveMonthlyActives(@Param("applicationId") UUID paramUUID, @Param("start") int paramInt1, @Param("end") int paramInt2);
  
  @Select({"SELECT applications_id AS applicationId, day AS period, COUNT(users_id) AS count FROM raw_application_daily_active_users GROUP BY applications_id, day"})
  List<IntervalCount> retrieveRawApplicationDailyActives();
  
  @Select({"SELECT applications_id AS applicationId, month AS period, COUNT(users_id) AS count FROM raw_application_monthly_active_users GROUP BY applications_id, month"})
  List<IntervalCount> retrieveRawApplicationMonthlyActives();
  
  @Select({"SELECT day AS period, COUNT(users_id) AS count FROM raw_global_daily_active_users GROUP BY day"})
  List<IntervalCount> retrieveRawGlobalDailyActives();
  
  @Select({"SELECT day AS period, COUNT(users_id) as count FROM raw_global_daily_active_users WHERE day = #{day} GROUP BY day"})
  IntervalCount retrieveRawGlobalDailyActivesByDay(@Param("day") int paramInt);
  
  @Select({"SELECT month AS period, COUNT(users_id) AS count FROM raw_global_monthly_active_users GROUP BY month"})
  List<IntervalCount> retrieveRawGlobalMonthlyActives();
  
  @Select({"SELECT month AS period, COUNT(users_id) as count FROM raw_global_monthly_active_users WHERE month = #{month} GROUP by month"})
  IntervalCount retrieveRawGlobalMonthlyActivesByMonth(@Param("month") int paramInt);
  
  List<DisplayableRawLogin> retrieveRawLoginsByCriteria(LoginRecordSearchCriteria paramLoginRecordSearchCriteria);
  
  int retrieveRawLoginsByCriteriaCount(LoginRecordSearchCriteria paramLoginRecordSearchCriteria);
  
  void upsertHourly(@Param("counts") List<IntervalCount> paramList);
  
  void upsertRawApplicationDailyActives(@Param("users") List<IntervalUser> paramList);
  
  void upsertRawApplicationMonthlyActives(@Param("users") List<IntervalUser> paramList);
  
  void upsertRawGlobalDailyActives(@Param("users") List<IntervalUser> paramList);
  
  void upsertRawGlobalMonthlyActives(@Param("users") List<IntervalUser> paramList);
}
