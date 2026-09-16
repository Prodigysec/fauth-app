package io.fusionauth.api.license;

import com.google.inject.Inject;
import com.inversoft.configuration.InversoftConfiguration;
import com.inversoft.license.v2.BaseConfigurationLicenseManager;
import io.fusionauth.api.domain.InstanceMapper;

public class FusionAuthLicenseManager extends BaseConfigurationLicenseManager {
  private final InstanceMapper instanceMapper;
  
  @Inject
  public FusionAuthLicenseManager(InversoftConfiguration paramInversoftConfiguration, InstanceMapper paramInstanceMapper) {
    super(paramInversoftConfiguration);
    this.instanceMapper = paramInstanceMapper;
  }
  
  public void updateLicenseText(String paramString) {
    this.instanceMapper.updateLicenseText(paramString);
  }
  
  protected String lookupLicenseTextLocally() {
    return this.instanceMapper.retrieveLicenseText();
  }
}
