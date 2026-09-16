package io.fusionauth.app.action.admin.system;

import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.domain.RegistrationCountMapper;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.service.application.ApplicationService;
import io.fusionauth.api.service.lock.ResetDistributedLock;
import io.fusionauth.api.service.reindex.ReindexService;
import io.fusionauth.api.service.system.ResetService;
import io.fusionauth.api.service.system.SetupService;
import io.fusionauth.api.service.system.SystemConfigurationService;
import io.fusionauth.api.service.user.DefaultUserService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.User;
import io.fusionauth.http.server.HTTPContext;
import java.time.LocalDate;
import java.time.Month;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Redirect(code = "exit", uri = "/admin/")
public abstract class BaseResetAction extends BaseAction {
  private static final Logger logger = LoggerFactory.getLogger(BaseResetAction.class);
  
  protected final String adminEmail;
  
  protected final String adminPassword;
  
  protected final ApplicationService applicationService;
  
  protected final HTTPContext context;
  
  protected final ReindexService reindexService;
  
  protected final SetupService setupService;
  
  protected final SystemConfigurationService systemConfigurationService;
  
  protected final UserMapper userMapper;
  
  private final ApplicationMapper applicationMapper;
  
  private final LoginMapper loginMapper;
  
  private final RegistrationCountMapper registrationCountMapper;
  
  private final ResetDistributedLock resetDistributedLock;
  
  private final ResetService resetService;
  
  public String confirm;
  
  protected BaseResetAction(FrontEndSupport paramFrontEndSupport, ApplicationMapper paramApplicationMapper, ApplicationService paramApplicationService, LoginMapper paramLoginMapper, ResetService paramResetService, ResetDistributedLock paramResetDistributedLock, RegistrationCountMapper paramRegistrationCountMapper, ReindexService paramReindexService, HTTPContext paramHTTPContext, SetupService paramSetupService, SystemConfigurationService paramSystemConfigurationService, UserMapper paramUserMapper) {
    super(paramFrontEndSupport);
    this.adminEmail = paramFrontEndSupport.configuration.properties().getProperty("fusionauth-app.demo.email", "admin@fusionauth.io");
    this.adminPassword = paramFrontEndSupport.configuration.properties().getProperty("fusionauth-app.demo.password", "password");
    this.applicationMapper = paramApplicationMapper;
    this.applicationService = paramApplicationService;
    this.loginMapper = paramLoginMapper;
    this.registrationCountMapper = paramRegistrationCountMapper;
    this.reindexService = paramReindexService;
    this.resetService = paramResetService;
    this.resetDistributedLock = paramResetDistributedLock;
    this.context = paramHTTPContext;
    this.setupService = paramSetupService;
    this.systemConfigurationService = paramSystemConfigurationService;
    this.userMapper = paramUserMapper;
  }
  
  protected void clearLoginMetrics() {
    this.applicationMapper.retrieveAllIgnoreActive(null).forEach(paramApplication -> {
          this.loginMapper.deleteHourlyLoginsForApplication(paramApplication.id);
          this.loginMapper.deleteMonthlyActiveForApplication(paramApplication.id);
          this.loginMapper.deleteDailyActiveForApplication(paramApplication.id);
        });
    this.loginMapper.deleteMonthlyActiveForGlobal();
    this.loginMapper.deleteDailyActiveForGlobal();
    this.registrationCountMapper.deleteCountsForGlobal();
  }
  
  protected void resetSystem() {
    this.resetService.reset("salted-pbkdf2-hmac-sha256", 24000, true, this.resetDistributedLock, true);
    User user = (new User()).with(paramUser -> paramUser.email = this.adminEmail).with(paramUser -> paramUser.password = this.adminPassword).with(paramUser -> paramUser.firstName = "Erlich").with(paramUser -> paramUser.lastName = "Bachman").with(paramUser -> paramUser.birthDate = LocalDate.of(1981, Month.JUNE, 4)).with(paramUser -> paramUser.data.put("Company", "Aviato")).with(paramUser -> paramUser.data.put("user_type", "iconoclast"));
    DefaultUserService.handleUserPrimaryIdentities(user);
    this.setupService.setup(user, this.frontEndSupport
        .getTrustedClientIPAddress(), false, null, false);
    logger.info("Delete the entire Entity search index and rebuild it");
    try {
      this.reindexService.reindexEntities().join();
    } catch (InterruptedException interruptedException) {}
    logger.info("Delete the entire User search index and rebuild it");
    try {
      this.reindexService.reindexUsers().join();
    } catch (InterruptedException interruptedException) {}
  }
  
  protected void validateConfirmation() {
    if (this.confirm == null) {
      this.frontEndSupport.addFieldError("confirm", "[missing]confirm", new Object[0]);
    } else if (!this.confirm.equals("RESET")) {
      this.frontEndSupport.addFieldError("confirm", "[invalid]confirm", new Object[0]);
    } 
  }
}
