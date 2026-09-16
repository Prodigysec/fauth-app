package io.fusionauth.app.service.security;

import com.google.inject.Inject;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultTwoFactorFrontendService implements TwoFactorFrontendService {
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final ReactorStatusService reactorStatusService;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultTwoFactorFrontendService(ExternalIdentifierReaderService paramExternalIdentifierReaderService, ReactorStatusService paramReactorStatusService, UserReaderService paramUserReaderService) {
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.reactorStatusService = paramReactorStatusService;
    this.userReader = paramUserReaderService;
  }
  
  public List<String> availableMethodsToConfigureForUser(Tenant paramTenant, User paramUser) {
    ArrayList<String> arrayList = new ArrayList();
    if (paramTenant.multiFactorConfiguration.authenticator.enabled)
      arrayList.add("authenticator"); 
    if (ReactorStatusValidator.isLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.advancedMultiFactorAuthentication)) {
      if (paramTenant.multiFactorConfiguration.email.enabled)
        arrayList.add("email"); 
      if (paramTenant.multiFactorConfiguration.sms.enabled || paramTenant.multiFactorConfiguration.voice.enabled)
        arrayList.add("sms"); 
    } 
    return arrayList;
  }
  
  public List<String> availableMethodsToUseAtLogin(Tenant paramTenant, User paramUser) {
    List<String> list1 = (List)paramUser.twoFactor.methods.stream().map(paramTwoFactorMethod -> paramTwoFactorMethod.method).distinct().collect(Collectors.toList());
    List<String> list2 = availableMethodsToConfigureForUser(paramTenant, paramUser);
    list1.removeIf(paramString -> !paramList.contains(paramString));
    return list1;
  }
  
  public ExternalIdentifierReaderService.ValidationResult verifyTwoFactorId(Tenant paramTenant, String paramString) {
    ExternalIdentifierReaderService.ValidationResult validationResult1 = new ExternalIdentifierReaderService.ValidationResult();
    ExternalIdentifierReaderService.ValidationResult validationResult2 = this.externalIdentifierReader.validate(paramTenant, paramString, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TwoFactor });
    if (validationResult2.id != null) {
      validationResult1.tenant = validationResult2.tenant;
      validationResult1.id = validationResult2.id;
      validationResult1.user = this.userReader.retrieveById(paramTenant.id, validationResult2.id.userId);
    } 
    return validationResult1;
  }
}
