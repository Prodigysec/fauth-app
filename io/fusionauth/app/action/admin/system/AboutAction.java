package io.fusionauth.app.action.admin.system;

import com.google.inject.Inject;
import io.fusionauth.api.domain.ProductInformation;
import io.fusionauth.api.service.system.SystemConfigurationService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true)
public class AboutAction extends BaseAction {
  private final SystemConfigurationService systemConfigurationService;
  
  @FTLVariable
  public ProductInformation productInformation;
  
  @Inject
  public AboutAction(FrontEndSupport paramFrontEndSupport, SystemConfigurationService paramSystemConfigurationService) {
    super(paramFrontEndSupport);
    this.systemConfigurationService = paramSystemConfigurationService;
  }
  
  public String get() {
    this.productInformation = this.systemConfigurationService.retrieveProductInformation();
    return "input";
  }
}
