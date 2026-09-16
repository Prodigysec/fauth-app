package io.fusionauth.api.service.moderation.cleanspeak;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import com.inversoft.error.Errors;
import com.inversoft.json.ToString;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.ProxyInfoSupplier;
import io.fusionauth.api.service.moderation.ModerationService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.CleanSpeakConfiguration;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Integration;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanSpeakModerationService implements ModerationService {
  private static final Logger logger = LoggerFactory.getLogger(CleanSpeakModerationService.class);
  
  private final CleanSpeakConfiguration cleanSpeakConfiguration;
  
  private final CleanSpeakClient client;
  
  @Inject
  public CleanSpeakModerationService(Cache<String, Integration> paramCache, ProxyInfoSupplier paramProxyInfoSupplier) {
    this.cleanSpeakConfiguration = (CleanSpeakConfiguration)paramCache.get("cleanspeak");
    if (this.cleanSpeakConfiguration != null && this.cleanSpeakConfiguration.enabled) {
      this.client = new CleanSpeakClient(this.cleanSpeakConfiguration.apiKey, this.cleanSpeakConfiguration.url.toString(), paramProxyInfoSupplier);
    } else {
      this.client = null;
    } 
  }
  
  public ModerateResponse moderate(User paramUser) {
    String str = paramUser.username;
    if (this.client == null || str == null || !this.cleanSpeakConfiguration.usernameModeration.enabled)
      return null; 
    paramUser.cleanSpeakId = UUID.randomUUID();
    return callModerate(paramUser.id, str, paramUser.cleanSpeakId);
  }
  
  public ModerateResponse moderate(UserRegistration paramUserRegistration, Application paramApplication, UUID paramUUID) {
    if (this.client == null || paramUserRegistration.username == null)
      return null; 
    if (paramApplication.cleanSpeakConfiguration == null || !paramApplication.cleanSpeakConfiguration.usernameModeration.enabled)
      return null; 
    paramUserRegistration.cleanSpeakId = UUID.randomUUID();
    return callModerate(paramUUID, paramUserRegistration.username, paramUserRegistration.cleanSpeakId);
  }
  
  private ModerateResponse callModerate(UUID paramUUID1, String paramString, UUID paramUUID2) {
    UUID uUID = (paramUUID1 != null) ? paramUUID1 : UUID.randomUUID();
    ModerateRequest.Content content = new ModerateRequest.Content(this.cleanSpeakConfiguration.usernameModeration.applicationId, ZonedDateTime.now(ZoneOffset.UTC), "FusionAuth", null, null, paramString, uUID, new ModerateRequest.Content.ContentPart[] { new ModerateRequest.Content.ContentPart(paramString) });
    ClientResponse<ModerateResponse, Errors> clientResponse = this.client.moderateCreate(paramUUID2, new ModerateRequest(content, null, false));
    if (clientResponse.exception != null || clientResponse.status != 200)
      if (clientResponse.exception != null) {
        logger.error("Error while calling CleanSpeak", clientResponse.exception);
        EventLogHelper.create(new EventLog(EventLogType.Error, "Error while calling CleanSpeak", clientResponse.exception));
      } else {
        logger.error("CleanSpeak error. Unable to moderate username [{}]. CleanSpeak returned status code [{}] with errors\n{}", new Object[] { paramString, 
              Integer.valueOf(clientResponse.status), clientResponse.errorResponse });
        EventLogHelper.create(new EventLog(EventLogType.Error, "Unable to moderate username [" + paramString + "].\n\nCleanSpeak returned status code [" + clientResponse.status + "].\n\nError response:\n" + 
              
              ToString.toString(clientResponse.errorResponse)));
      }  
    return (ModerateResponse)clientResponse.successResponse;
  }
}
