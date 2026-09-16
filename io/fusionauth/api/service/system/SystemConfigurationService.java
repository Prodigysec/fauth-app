package io.fusionauth.api.service.system;

import com.inversoft.error.Errors;
import io.fusionauth.api.domain.ProductInformation;
import io.fusionauth.domain.SystemConfiguration;

public interface SystemConfigurationService {
  boolean isUsageStatsEnabled();
  
  SystemConfiguration retrieve();
  
  ProductInformation retrieveProductInformation();
  
  void update(SystemConfiguration paramSystemConfiguration);
  
  Errors validate(SystemConfiguration paramSystemConfiguration);
}
