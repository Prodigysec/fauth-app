package io.fusionauth.api.service.user;

import com.google.inject.Inject;
import io.fusionauth.api.domain.ZonedDateTimeWrapper;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserReaper implements Runnable {
  private final ApplicationReaderService applicationReader;
  
  private final TenantReaderService tenantReader;
  
  private final UserService userService;
  
  @Inject
  public UserReaper(ApplicationReaderService paramApplicationReaderService, TenantReaderService paramTenantReaderService, UserService paramUserService) {
    this.applicationReader = paramApplicationReaderService;
    this.tenantReader = paramTenantReaderService;
    this.userService = paramUserService;
  }
  
  public void run() {
    Map map = (Map)this.tenantReader.retrieveAll().stream().collect(Collectors.toMap(paramTenant -> paramTenant.id, paramTenant -> paramTenant));
    ZonedDateTime zonedDateTime = ZonedDateTimeWrapper.now(ZoneOffset.UTC);
    map.values().forEach(paramTenant -> {
          if (paramTenant.familyConfiguration.enabled && paramTenant.familyConfiguration.deleteOrphanedAccounts) {
            ZonedDateTime zonedDateTime = paramZonedDateTime.minusDays(paramTenant.familyConfiguration.deleteOrphanedAccountsDays);
            this.userService.deleteUnverifiedChildren(paramTenant, zonedDateTime);
          } 
          if (paramTenant.userDeletePolicy.unverified.enabled && (paramTenant.emailConfiguration.verifyEmail || paramTenant.phoneConfiguration.verifyPhoneNumber)) {
            ZonedDateTime zonedDateTime = paramZonedDateTime.minusDays(paramTenant.userDeletePolicy.unverified.numberOfDaysToRetain);
            this.userService.deleteUnverifiedUsers(paramTenant, paramTenant.userDeletePolicy.unverified.enabledInstant, zonedDateTime);
          } 
        });
    List<Application> list = this.applicationReader.retrieveAll(null, Collections.emptySet());
    list.forEach(paramApplication -> {
          if (paramApplication.verifyRegistration && paramApplication.registrationDeletePolicy.unverified.enabled) {
            ZonedDateTime zonedDateTime = paramZonedDateTime.minusDays(paramApplication.registrationDeletePolicy.unverified.numberOfDaysToRetain);
            this.userService.deleteUnverifiedRegistrations((Tenant)paramMap.get(paramApplication.tenantId), paramApplication, paramApplication.registrationDeletePolicy.unverified.enabledInstant, zonedDateTime);
          } 
        });
  }
}
