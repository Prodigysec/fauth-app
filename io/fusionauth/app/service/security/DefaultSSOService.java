package io.fusionauth.app.service.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.StringTools;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.system.CipherService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.http.Cookie;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;

public class DefaultSSOService implements SSOService {
  private final CipherService cipherService;
  
  private final FusionAuthClient client;
  
  private final RefreshTokenService refreshTokenService;
  
  @Inject
  public DefaultSSOService(CipherService paramCipherService, FusionAuthClient paramFusionAuthClient, RefreshTokenService paramRefreshTokenService) {
    this.cipherService = paramCipherService;
    this.client = paramFusionAuthClient;
    this.refreshTokenService = paramRefreshTokenService;
  }
  
  public String buildMultiUserCookieName(Tenant paramTenant, String paramString1, UUID paramUUID, String paramString2) {
    String str = String.valueOf(paramTenant.id) + String.valueOf(paramTenant.id);
    return paramString2 + "." + paramString2;
  }
  
  public SSOService.CookieValue buildTrustedDeviceCookie(Tenant paramTenant, String paramString, UUID paramUUID, boolean paramBoolean) {
    try {
      String str = buildMultiUserCookieName(paramTenant, paramString, paramUUID, "fusionauth.trusted-device");
      UUID uUID = (paramUUID != null) ? paramUUID : UUID.randomUUID();
      return new SSOService.CookieValue(str, Base64.getUrlEncoder()
          .withoutPadding()

          
          .encodeToString(this.cipherService.encrypt(ByteBuffer.allocate(26)
              
              .put((byte)1)
              .putLong(uUID.getMostSignificantBits())
              .putLong(uUID.getLeastSignificantBits())
              .putLong(ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli())
              .put((byte)(paramBoolean ? 1 : 0))
              .array())));
    } catch (Exception exception) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Unexpected error while encrypting a [fusionauth.trusted-device] cookie.", exception));
      return null;
    } 
  }
  
  public SSOService.CookieValue buildWebAuthnReAuthenticationCookie(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2) {
    try {
      String str = buildMultiUserCookieName(paramTenant, null, paramUUID1, "fusionauth.webauthn-reauth");
      byte b = (paramUUID2 != null) ? 41 : 25;
      ByteBuffer byteBuffer = ByteBuffer.allocate(b).put((byte)1).putLong(paramUUID1.getMostSignificantBits()).putLong(paramUUID1.getLeastSignificantBits()).putLong(ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli());
      if (paramUUID2 != null)
        byteBuffer.putLong(paramUUID2.getMostSignificantBits())
          .putLong(paramUUID2.getLeastSignificantBits()); 
      return new SSOService.CookieValue(str, Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(this.cipherService.encrypt(byteBuffer.array())));
    } catch (Exception exception) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Unexpected error while building a [fusionauth.webauthn-reauth] cookie.", exception));
      return null;
    } 
  }
  
  public SSOService.SSOSession getSession(Tenant paramTenant, Cookie paramCookie) {
    SSOService.SSOSession sSOSession = new SSOService.SSOSession();
    buildSession(sSOSession, paramTenant, paramCookie);
    String str = sSOSession.tokens.get(paramTenant.id);
    if (str == null) {
      updateCookieValue(sSOSession, paramCookie);
      return sSOSession;
    } 
    User user = null;
    if (sSOSession.refreshToken != null) {
      this.client.setTenantId(paramTenant.id);
      ClientResponse<UserResponse, Errors> clientResponse = this.client.retrieveUser(sSOSession.refreshToken.userId);
      if (clientResponse.wasSuccessful())
        user = ((UserResponse)clientResponse.getSuccessResponse()).user; 
    } 
    if (user != null && user.expiry != null && user.expiry.isBefore(ZonedDateTime.now(ZoneOffset.UTC)))
      user = null; 
    if (user == null) {
      sSOSession.tokens.remove(paramTenant.id);
      updateCookieValue(sSOSession, paramCookie);
      return sSOSession;
    } 
    sSOSession.id = sSOSession.refreshToken.id;
    sSOSession.user = (new User(user)).secure().sort();
    return sSOSession;
  }
  
  public SSOService.TrustedDeviceState getTrustedDeviceState(List<Cookie> paramList, Tenant paramTenant, String paramString, UUID paramUUID) {
    if (!paramList.isEmpty()) {
      String str = (StringTools.isTrimmedEmpty(paramString) && paramUUID == null) ? null : buildMultiUserCookieName(paramTenant, paramString, paramUUID, "fusionauth.trusted-device");
      for (Cookie cookie : paramList) {
        if (str != null && 
          !cookie.getName().equals(str))
          continue; 
        if (cookie.getName().startsWith("fusionauth.trusted-device."))
          try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(this.cipherService.decrypt(Base64.getUrlDecoder()
                  .decode(cookie.getValue().getBytes(StandardCharsets.UTF_8))));
            byte b = byteBuffer.get();
            UUID uUID = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
            if (paramUUID != null && 
              !paramUUID.equals(uUID))
              continue; 
            long l = byteBuffer.getLong() + paramTenant.ssoConfiguration.deviceTrustTimeToLiveInSeconds * 1000L;
            boolean bool = (byteBuffer.get() == 1) ? true : false;
            if (l > System.currentTimeMillis()) {
              if (!StringTools.isTrimmedEmpty(paramString))
                return bool ? SSOService.TrustedDeviceState.TrustedAndVerified : SSOService.TrustedDeviceState.Trusted; 
              return (paramUUID == null) ? 
                SSOService.TrustedDeviceState.MaybeTrusted : (
                bool ? SSOService.TrustedDeviceState.TrustedAndVerified : SSOService.TrustedDeviceState.Trusted);
            } 
          } catch (Exception exception) {} 
      } 
    } 
    return SSOService.TrustedDeviceState.Untrusted;
  }
  
  public SSOService.WebAuthnReAuthenticationCredential getWebAuthnReAuthenticationCredential(List<Cookie> paramList, Tenant paramTenant, UUID paramUUID) {
    List<SSOService.WebAuthnReAuthenticationCredential> list = _getWebAuthnReAuthenticationCredentials(paramList, paramTenant, paramUUID);
    return list.isEmpty() ? null : list.get(0);
  }
  
  public List<SSOService.WebAuthnReAuthenticationCredential> getWebAuthnReAuthenticationCredentials(List<Cookie> paramList, Tenant paramTenant) {
    return _getWebAuthnReAuthenticationCredentials(paramList, paramTenant, null);
  }
  
  public SSOService.NewDeviceResult handleNewDevice(List<Cookie> paramList, Tenant paramTenant, UUID paramUUID) {
    SSOService.NewDeviceResult newDeviceResult = new SSOService.NewDeviceResult();
    String str = String.valueOf(paramTenant.id) + String.valueOf(paramTenant.id);
    newDeviceResult.cookieName = "fusionauth.known-device." + Base64.getUrlEncoder().withoutPadding().encodeToString(digest().digest(str.getBytes(StandardCharsets.UTF_8)));
    newDeviceResult.cookie = paramList.stream().filter(paramCookie -> paramCookie.name.equals(paramNewDeviceResult.cookieName)).findFirst().orElse(null);
    if (newDeviceResult.cookie != null)
      try {
        ByteBuffer byteBuffer = ByteBuffer.wrap(this.cipherService.decrypt(Base64.getUrlDecoder()
              .decode(newDeviceResult.cookie.value.getBytes(StandardCharsets.UTF_8))));
        newDeviceResult.version = byteBuffer.get();
        newDeviceResult.actualUserId = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
      } catch (Exception exception) {
        EventLogHelper.create(new EventLog(EventLogType.Error, "Unexpected error while decrypting a [fusionauth.known-device] cookie.", exception));
      }  
    newDeviceResult.expectedUserId = paramUUID;
    try {
      newDeviceResult


        
        .cookieValue = Base64.getUrlEncoder().withoutPadding().encodeToString(this.cipherService.encrypt(ByteBuffer.allocate(25)
            
            .put((byte)1)
            .putLong(newDeviceResult.expectedUserId.getMostSignificantBits())
            .putLong(newDeviceResult.expectedUserId.getLeastSignificantBits())
            
            .putLong(ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli())
            .array()));
    } catch (Exception exception) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Unexpected error while encrypting a [fusionauth.known-device] cookie.", exception));
    } 
    return newDeviceResult;
  }
  
  public void login(SSOService.SSOSession paramSSOSession, Cookie paramCookie, Tenant paramTenant, User paramUser, RefreshToken.MetaData paramMetaData, ZonedDateTime paramZonedDateTime, AuthenticationType paramAuthenticationType, String paramString) {
    paramSSOSession.user = paramUser;
    if (paramSSOSession.tokens.containsKey(paramTenant.id)) {
      setAuthTime(paramSSOSession.refreshToken.metaData, paramZonedDateTime);
      setAuthenticationType(paramSSOSession.refreshToken.metaData, paramAuthenticationType);
      this.refreshTokenService.refreshSSOSessionToken(paramSSOSession.refreshToken, paramString);
      paramSSOSession.tokens.put(paramTenant.id, paramSSOSession.refreshToken.token);
      paramSSOSession.id = paramSSOSession.refreshToken.id;
    } else {
      if (paramMetaData == null)
        paramMetaData = new RefreshToken.MetaData(); 
      if (paramMetaData.data != null)
        paramMetaData.data.remove("authentication_type"); 
      setAuthTime(paramMetaData, paramZonedDateTime);
      setAuthenticationType(paramMetaData, paramAuthenticationType);
      RefreshToken refreshToken = this.refreshTokenService.createSSOSessionToken(paramUser, paramMetaData, paramString);
      paramSSOSession.tokens.put(paramTenant.id, refreshToken.token);
      paramSSOSession.id = refreshToken.id;
      paramSSOSession.refreshToken = refreshToken;
    } 
    updateCookieValue(paramSSOSession, paramCookie);
  }
  
  public void logout(SSOService.SSOSession paramSSOSession, Cookie paramCookie, Tenant paramTenant, EventInfo paramEventInfo) {
    String str = paramSSOSession.tokens.remove(paramTenant.id);
    if (str != null)
      this.refreshTokenService.revokeSSOSessionToken(str, paramEventInfo); 
    updateCookieValue(paramSSOSession, paramCookie);
  }
  
  private List<SSOService.WebAuthnReAuthenticationCredential> _getWebAuthnReAuthenticationCredentials(List<Cookie> paramList, Tenant paramTenant, UUID paramUUID) {
    ArrayList<SSOService.WebAuthnReAuthenticationCredential> arrayList = new ArrayList();
    if (!paramList.isEmpty())
      for (Cookie cookie : paramList) {
        if (cookie.getName().startsWith("fusionauth.webauthn-reauth."))
          try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(this.cipherService.decrypt(Base64.getUrlDecoder()
                  .decode(cookie.getValue().getBytes(StandardCharsets.UTF_8))));
            byte b = byteBuffer.get();
            UUID uUID1 = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
            if (paramUUID != null && !uUID1.equals(paramUUID))
              continue; 
            String str = buildMultiUserCookieName(paramTenant, null, uUID1, "fusionauth.webauthn-reauth");
            if (!cookie.getName().equals(str))
              continue; 
            byteBuffer.getLong();
            UUID uUID2 = null;
            if (byteBuffer.position() < byteBuffer.capacity())
              uUID2 = new UUID(byteBuffer.getLong(), byteBuffer.getLong()); 
            arrayList.add(new SSOService.WebAuthnReAuthenticationCredential(cookie.getName(), uUID2, uUID1));
          } catch (Exception exception) {} 
      }  
    return arrayList;
  }
  
  private void buildSession(SSOService.SSOSession paramSSOSession, Tenant paramTenant, Cookie paramCookie) {
    try {
      deserialize(paramSSOSession, paramCookie);
    } catch (Exception exception) {
      paramCookie.value = null;
    } 
    if (paramSSOSession.tokens != null && paramTenant != null) {
      String str = paramSSOSession.tokens.get(paramTenant.id);
      if (str != null) {
        paramSSOSession.refreshToken = this.refreshTokenService.retrieveRefreshToken(str);
        if (paramSSOSession.refreshToken != null) {
          if (paramSSOSession.refreshToken.isExpired(paramTenant, null)) {
            paramSSOSession.refreshToken = null;
            paramSSOSession.tokens.remove(paramTenant.id);
            paramSSOSession.loggedOut = true;
          } 
        } else {
          paramSSOSession.tokens.remove(paramTenant.id);
          paramSSOSession.loggedOut = true;
        } 
      } 
    } 
  }
  
  private void deserialize(SSOService.SSOSession paramSSOSession, Cookie paramCookie) throws GeneralSecurityException, JsonProcessingException {
    String str = paramCookie.value;
    if (str == null || str.isEmpty())
      return; 
    byte[] arrayOfByte1 = Base64.getUrlDecoder().decode(str.getBytes(StandardCharsets.UTF_8));
    byte b = arrayOfByte1[0];
    if (b != 1 && b != 2)
      return; 
    byte[] arrayOfByte2 = this.cipherService.decrypt(Arrays.copyOfRange(arrayOfByte1, 1, arrayOfByte1.length));
    ByteBuffer byteBuffer = ByteBuffer.wrap(arrayOfByte2);
    if (b == 2) {
      byteBuffer.get();
      byteBuffer.getLong();
    } 
    while (byteBuffer.hasRemaining()) {
      UUID uUID = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
      short s = byteBuffer.getShort();
      byte[] arrayOfByte = new byte[s];
      for (byte b1 = 0; b1 < s; b1++)
        arrayOfByte[b1] = byteBuffer.get(); 
      paramSSOSession.tokens.put(uUID, new String(arrayOfByte));
    } 
  }
  
  private MessageDigest digest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (Exception exception) {
      throw new ErrorException(exception, new Object[0]);
    } 
  }
  
  private void setAuthTime(RefreshToken.MetaData paramMetaData, ZonedDateTime paramZonedDateTime) {
    if (paramMetaData.data == null)
      paramMetaData.data = new HashMap<>(1); 
    paramMetaData.data.put("auth_time", Long.valueOf(paramZonedDateTime.toEpochSecond()));
  }
  
  private void setAuthenticationType(RefreshToken.MetaData paramMetaData, AuthenticationType paramAuthenticationType) {
    if (paramAuthenticationType == null || paramAuthenticationType == AuthenticationType.PING || paramAuthenticationType == AuthenticationType.REFRESH_TOKEN)
      return; 
    if (paramMetaData.data == null)
      paramMetaData.data = new HashMap<>(1); 
    paramMetaData.data.put("authentication_type", paramAuthenticationType.name());
  }
  
  private void updateCookieValue(SSOService.SSOSession paramSSOSession, Cookie paramCookie) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
    byteBuffer.put((byte)0);
    byteBuffer.putLong(0L);
    for (UUID uUID : paramSSOSession.tokens.keySet()) {
      byteBuffer.putLong(uUID.getMostSignificantBits());
      byteBuffer.putLong(uUID.getLeastSignificantBits());
      String str = paramSSOSession.tokens.get(uUID);
      byteBuffer.putShort((short)str.length());
      byteBuffer.put(str.getBytes(StandardCharsets.UTF_8));
    } 
    try {
      byte[] arrayOfByte1 = Arrays.copyOfRange(byteBuffer.array(), 0, byteBuffer.position());
      byte[] arrayOfByte2 = this.cipherService.encrypt(arrayOfByte1);
      byte[] arrayOfByte3 = new byte[arrayOfByte2.length + 1];
      arrayOfByte3[0] = 2;
      System.arraycopy(arrayOfByte2, 0, arrayOfByte3, 1, arrayOfByte2.length);
      paramCookie.value = Base64.getUrlEncoder().withoutPadding().encodeToString(arrayOfByte3);
    } catch (Exception exception) {
      throw new ErrorException(exception, new Object[0]);
    } 
  }
}
