package io.fusionauth.app.service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.domain.user.Preferences;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.client.LambdaDelegate;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.ReactorResponse;
import io.fusionauth.domain.api.user.RegistrationRequest;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultUserPreferencesFrontendService implements UserPreferencesFrontendService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultUserPreferencesFrontendService.class);
  
  private final FrontEndSupport frontEndSupport;
  
  private final LambdaDelegate superDelegate;
  
  @Inject
  public DefaultUserPreferencesFrontendService(FrontEndSupport paramFrontEndSupport) {
    this.frontEndSupport = paramFrontEndSupport;
    Objects.requireNonNull(paramFrontEndSupport);
    this.superDelegate = new LambdaDelegate(paramFrontEndSupport.fusionAuthClientProvider.get(), paramClientResponse -> paramClientResponse.successResponse, paramFrontEndSupport::frontEndErrorHandling);
  }
  
  public void dismissLicenseCTA(User paramUser) {
    UserRegistration userRegistration = paramUser.getRegistrationForApplication(Application.FUSIONAUTH_APP_ID);
    if (userRegistration != null) {
      userRegistration.data.put("preferences", (new Preferences())
          .with(paramPreferences -> paramPreferences.announcements = Map.of("licenseCTA", "dismissed")));
      this.superDelegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateRegistration(paramUser.id, new RegistrationRequest(this.frontEndSupport.buildEventInfo(), null, paramUserRegistration)));
    } 
  }
  
  public boolean showUserLicenseCTA(User paramUser) {
    ReactorStatus reactorStatus = ((ReactorResponse)this.superDelegate.execute(FusionAuthClient::retrieveReactorStatus)).status;
    if (paramUser != null && !reactorStatus.licensed) {
      UserRegistration userRegistration = paramUser.getRegistrationForApplication(Application.FUSIONAUTH_APP_ID);
      if (userRegistration != null && userRegistration.roles.contains("admin")) {
        Optional<Preferences> optional = getUserPreferences(userRegistration);
        if (optional.isPresent() && ((Preferences)optional.get()).announcements != null) {
          Map<Object, Object> map = ((Preferences)optional.get()).announcements;
          return !"dismissed".equals(map.get("licenseCTA"));
        } 
        return true;
      } 
    } 
    return false;
  }
  
  private Optional<Preferences> getUserPreferences(UserRegistration paramUserRegistration) {
    try {
      ObjectMapper objectMapper = this.frontEndSupport.objectMapper;
      return Optional.ofNullable((Preferences)objectMapper.convertValue(paramUserRegistration.data.get("preferences"), Preferences.class));
    } catch (IllegalArgumentException illegalArgumentException) {
      logger.warn("Failed to properly parse user preferences");
      return Optional.empty();
    } 
  }
}
