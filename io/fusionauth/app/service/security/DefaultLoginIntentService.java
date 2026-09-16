package io.fusionauth.app.service.security;

import com.google.inject.Inject;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.app.primeframework.PostAuthenticationStep;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.http.Cookie;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLoginIntentService implements LoginIntentService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultLoginIntentService.class);
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final ExternalIdentifierService externalIdentifierService;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultLoginIntentService(ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, UserReaderService paramUserReaderService) {
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.externalIdentifierService = paramExternalIdentifierService;
    this.userReader = paramUserReaderService;
  }
  
  public String buildLoginIntent(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, PostAuthenticationStep paramPostAuthenticationStep, SSOService.RememberDeviceState paramRememberDeviceState, String paramString) {
    try {
      String str = this.externalIdentifierService.createLoginIntent(paramTenant, paramUUID2, paramUUID1, (new ExternalIdentifier.ExternalIdData())
          
          .setAttribute("postAuthenticationStep", paramPostAuthenticationStep.name())
          .setAttribute("rememberDeviceState", paramRememberDeviceState.name())
          .setAttribute("verificationId", paramString));
      ByteBuffer byteBuffer = ByteBuffer.allocate(51 + str.length()).put((byte)1).putLong(paramTenant.id.getMostSignificantBits()).putLong(paramTenant.id.getLeastSignificantBits()).putLong(paramUUID1.getMostSignificantBits()).putLong(paramUUID1.getLeastSignificantBits()).putLong(paramUUID2.getMostSignificantBits()).putLong(paramUUID2.getLeastSignificantBits()).putShort((short)str.length()).put(str.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(byteBuffer.array());
    } catch (Exception exception) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Unexpected error while building a [fusionauth.li] cookie.", exception));
      return null;
    } 
  }
  
  public void deleteByUserId(UUID paramUUID) {
    if (paramUUID != null)
      this.externalIdentifierService.deleteByUserId(paramUUID, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.LoginIntent }); 
  }
  
  public LoginIntentService.LoginIntent getLoginIntent(Tenant paramTenant, Application paramApplication, Cookie paramCookie) {
    if (paramCookie == null || paramCookie.getValue() == null || paramApplication == null)
      return null; 
    try {
      ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getUrlDecoder()
          .decode(paramCookie.getValue().getBytes(StandardCharsets.UTF_8)));
      byte b = byteBuffer.get();
      UUID uUID1 = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
      UUID uUID2 = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
      UUID uUID3 = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
      short s = byteBuffer.getShort();
      byte[] arrayOfByte = new byte[s];
      byteBuffer.get(arrayOfByte);
      String str = new String(arrayOfByte, StandardCharsets.UTF_8);
      if (!Objects.equals(paramTenant.id, uUID1)) {
        logger.debug("The current tenant Id [{}] does not match the cookie's tenant Id [{}] when attempting to get the login intent.", paramTenant.id, uUID1);
        return null;
      } 
      if (!Objects.equals(paramApplication.id, uUID2)) {
        logger.debug("The current application Id [{}] does not match the cookie's application Id [{}] when attempting to get the login intent", paramApplication.id, uUID2);
        return null;
      } 
      ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveByUserIdTypeAndApplicationId(uUID3, ExternalIdentifier.ExternalIdType.LoginIntent, uUID2);
      if (externalIdentifier == null) {
        logger.debug("No login intent exists for user [{}] and application with Id [{}].", uUID3, uUID2);
        return null;
      } 
      if (externalIdentifier.isExpired(paramTenant)) {
        logger.debug("Found login intent for user [{}] and application with Id [{}], but it was expired.", uUID3, uUID2);
        return null;
      } 
      if (!Objects.equals(str, externalIdentifier.id)) {
        logger.debug("Found login intent for user [{}] and application with Id [{}], but the cookie value did not match.", uUID3, uUID2);
        return null;
      } 
      User user = this.userReader.retrieveById(uUID1, uUID3);
      if (user == null) {
        logger.debug("User from login intent with Id [{}] not found.", uUID3);
        return null;
      } 
      if (user.expiry != null && user.expiry.isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
        logger.debug("User from login intent with Id [{}] was found but was expired.", uUID3);
        return null;
      } 
      return new LoginIntentService.LoginIntent(uUID1, uUID2, externalIdentifier.insertInstant, (new User(user)).secure().sort(), externalIdentifier.id, 
          PostAuthenticationStep.safeValueOf(externalIdentifier.getAttribute("postAuthenticationStep")), 
          SSOService.RememberDeviceState.safeValueOf(externalIdentifier.getAttribute("rememberDeviceState")), externalIdentifier
          .getAttribute("verificationId"));
    } catch (Exception exception) {
      logger.error("Error attempting to deserialize login intent cookie.", exception);
      return null;
    } 
  }
  
  public void updatePostAuthenticationStep(LoginIntentService.LoginIntent paramLoginIntent, PostAuthenticationStep paramPostAuthenticationStep, String paramString) {
    ExternalIdentifier externalIdentifier = paramLoginIntent.toExternalIdentifier();
    externalIdentifier.data.setAttribute("postAuthenticationStep", paramPostAuthenticationStep.name());
    if (paramString != null)
      externalIdentifier.data.setAttribute("verificationId", paramString); 
    this.externalIdentifierService.update(externalIdentifier);
  }
}
