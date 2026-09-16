package io.fusionauth.api.service.reactor;

import com.inversoft.license.v2.domain.LicenseContainer;
import io.fusionauth.api.domain.DatasetMapper;
import io.fusionauth.api.domain.api.reactor.BreachResult;
import io.fusionauth.domain.User;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

public interface ReactorService {
  static boolean isLicenseContainerInvalid(LicenseContainer paramLicenseContainer) {
    return (paramLicenseContainer == null || paramLicenseContainer.license() == null || paramLicenseContainer.license().invalidSchemes(Map.of()).size() > 0);
  }
  
  ActivateResult activate(String paramString1, String paramString2);
  
  void deactivate();
  
  boolean regenerate();
  
  BreachResult retrieveBreachResultForChange(User paramUser, String paramString);
  
  BreachResult retrieveBreachResultForLogin(User paramUser, String paramString);
  
  void setCommonPasswordDatasetVersion(ZonedDateTime paramZonedDateTime);
  
  void updateBreachMetrics(UUID paramUUID, BreachResult paramBreachResult);
  
  void updateCommonPasswordDataset();
  
  void updateDatasetLastUpdateInstant(DatasetMapper.DatasetName paramDatasetName, ZonedDateTime paramZonedDateTime);
  
  void updateStatusWithHealthCheck();
  
  ActivateResult updateStatusWithNewFeatures();
  
  boolean userRequiresCheck(User paramUser);
}
