package io.fusionauth.app.service.license;

import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;

public interface FrontEndLicensedFeaturesService {
  void disableLicensedFeatures(Tenant paramTenant);
  
  void disableLicensedFeatures(Application paramApplication);
}
