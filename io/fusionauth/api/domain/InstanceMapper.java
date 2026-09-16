package io.fusionauth.api.domain;

import java.time.ZonedDateTime;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface InstanceMapper {
  @Update({"UPDATE instance SET activate_instant = #{activateInstant}, license = #{licenseText}, license_id = #{licenseId}"})
  void activateLicense(@Param("activateInstant") ZonedDateTime paramZonedDateTime, @Param("licenseId") String paramString1, @Param("licenseText") String paramString2);
  
  @Update({"UPDATE instance SET activate_instant = NULL, license = NULL, license_id = NULL"})
  void removeLicense();
  
  @Select({"SELECT id, activate_instant, data AS dataFromDatabase, fips_enabled, reactor_health_checks, setup_complete FROM instance"})
  Instance retrieve();
  
  @Select({"SELECT internal_authentication_keys_id FROM instance LIMIT 1"})
  UUID retrieveInternalAuthenticationKeyId();
  
  @Select({"SELECT license_id FROM instance"})
  String retrieveLicenseId();
  
  @Select({"SELECT license FROM instance"})
  String retrieveLicenseText();
  
  @Select({"SELECT tenant_manager_applications_id FROM instance LIMIT 1"})
  UUID retrieveTenantManagerApplicationsId();
  
  @Update({"UPDATE instance SET setup_complete = TRUE"})
  void setupComplete();
  
  @Update({"UPDATE instance SET setup_complete = FALSE"})
  void setupIncomplete();
  
  @Update({"UPDATE instance SET activate_instant = #{activateInstant}"})
  void updateActivateInstant(@Param("activateInstant") ZonedDateTime paramZonedDateTime);
  
  @Update({"UPDATE instance SET data = #{dataToDatabase}"})
  void updateData(Instance paramInstance);
  
  @Update({"UPDATE instance SET fips_enabled = #{fipsEnabled} WHERE fips_enabled IS NULL"})
  int updateFIPS(boolean paramBoolean);
  
  @Update({"UPDATE instance SET license_id = #{licenseId}"})
  void updateLicenseId(@Param("licenseId") String paramString);
  
  @Update({"UPDATE instance SET license = #{licenseText}"})
  void updateLicenseText(@Param("licenseText") String paramString);
  
  @Update({"UPDATE instance SET reactor_health_checks = #{reactorHealthChecks}"})
  void updateReactorHealthChecks(ReactorHealthChecks paramReactorHealthChecks);
}
