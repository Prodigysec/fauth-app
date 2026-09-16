package io.fusionauth.app.action.support;

import com.google.inject.Inject;
import com.inversoft.support.service.SupportService;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.user.DefaultUserService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.GrantType;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.security.oauth.Tokens;
import org.primeframework.mvc.validation.ValidationMethod;

@Action("{authorizationCode}")
@List({@Redirect(uri = "/admin/"), @Redirect(code = "done", uri = "/support/login"), @Redirect(code = "invalid-code", uri = "/support/login")})
public class LoginAction extends BaseAction {
  private final ApplicationReaderService applicationReader;
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final ExternalIdentifierService externalIdentifierService;
  
  private final RefreshTokenService refreshTokenService;
  
  private final SupportService supportService;
  
  private final UserReaderService userReader;
  
  private final UserService userService;
  
  public String authorizationCode;
  
  public String one;
  
  public String three;
  
  public String two;
  
  @Inject
  public LoginAction(FrontEndSupport paramFrontEndSupport, ApplicationReaderService paramApplicationReaderService, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, RefreshTokenService paramRefreshTokenService, SupportService paramSupportService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramFrontEndSupport);
    this.applicationReader = paramApplicationReaderService;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.externalIdentifierService = paramExternalIdentifierService;
    this.refreshTokenService = paramRefreshTokenService;
    this.supportService = paramSupportService;
    this.userReader = paramUserReaderService;
    this.userService = paramUserService;
  }
  
  public String get() {
    if (!this.frontEndSupport.configuration.supportLoginEnabled())
      return "success"; 
    if (this.authorizationCode == null)
      return "input"; 
    ExternalIdentifierReaderService.ValidationResult validationResult = this.externalIdentifierReader.validate(this.codeCurrentTenant, this.authorizationCode, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.AuthorizedSupportId });
    if (validationResult.id == null) {
      this.frontEndSupport.addGeneralError("[InvalidSupportVerificationId]", new Object[0]);
      return "invalid-code";
    } 
    this.externalIdentifierService.deleteById(this.authorizationCode);
    completeLogin(validationResult.id.getAttribute("authorizedId"));
    this.zoneId = ZoneId.systemDefault();
    return "success";
  }
  
  public String post() {
    if (!this.frontEndSupport.configuration.supportLoginEnabled())
      return "success"; 
    if (this.one.endsWith("@fusionauth.io")) {
      String str1 = this.externalIdentifierService.createAuthorizedSupportId(this.codeCurrentTenant, (new ExternalIdentifier.ExternalIdData("authorizedId", this.one))
          
          .with(paramExternalIdData -> paramExternalIdData.device = null), 
          ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(5L));
      String str2 = this.frontEndSupport.getFusionAuthBaseURL() + "/support/login/" + this.frontEndSupport.getFusionAuthBaseURL();
      this.supportService.sendEmail(this.one, "FusionAuth Support Login Request", str2, "support@fusionauth.io", "FusionAuth Support", this.two, this.three);
    } 
    this.frontEndSupport.addGeneralInfo("authorizationCodeSent", new Object[0]);
    return "done";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.one == null || this.one.length() == 0)
      this.frontEndSupport.addFieldError("one", "[blank]one", new Object[0]); 
    if (this.two == null || this.two.length() == 0)
      this.frontEndSupport.addFieldError("two", "[blank]two", new Object[0]); 
    if (this.three == null || this.three.length() == 0)
      this.frontEndSupport.addFieldError("three", "[blank]three", new Object[0]); 
  }
  
  private void completeLogin(String paramString) {
    UUID uUID = this.frontEndSupport.tenantReader.retrieveFusionAuthTenantId();
    Tenant tenant = this.frontEndSupport.tenantReader.retrieveById(uUID);
    Application application = this.applicationReader.retrieveById(uUID, Application.FUSIONAUTH_APP_ID);
    User user = this.userReader.retrieveByLoginId(uUID, paramString, List.of(IdentityType.email));
    ApplicationRole applicationRole = this.applicationReader.retrieveRoleByName(null, Application.FUSIONAUTH_APP_ID, "admin");
    EventInfo eventInfo = this.frontEndSupport.buildEventInfo(null);
    if (user == null) {
      String str = SecurityTools.secureRandom();
      user = (new User()).with(paramUser -> paramUser.email = paramString).with(paramUser -> paramUser.password = paramString).with(paramUser -> paramUser.encryptionScheme = "salted-pbkdf2-hmac-sha256");
      DefaultUserService.handleUserPrimaryIdentities(user);
      this.userService.create(tenant, null, user, SendSetPasswordIdentityType.doNotSend, true, false, true, true, eventInfo, null);
      UserRegistration userRegistration = (new UserRegistration()).with(paramUserRegistration -> paramUserRegistration.applicationId = Application.FUSIONAUTH_APP_ID);
      this.userService.createRegistration(tenant, application, user, userRegistration, Collections.singletonList(applicationRole), false, true, false, eventInfo);
    } else {
      UserRegistration userRegistration = user.getRegistrationForApplication(Application.FUSIONAUTH_APP_ID);
      if (userRegistration == null) {
        userRegistration = (new UserRegistration()).with(paramUserRegistration -> paramUserRegistration.applicationId = Application.FUSIONAUTH_APP_ID);
        this.userService.createRegistration(tenant, application, user, userRegistration, Collections.singletonList(applicationRole), false, true, false, eventInfo);
      } else if (!userRegistration.roles.contains("admin")) {
        this.userService.updateRegistration(tenant, application, user, userRegistration, userRegistration, Collections.singletonList(applicationRole), false, eventInfo);
      } 
    } 
    if (!user.active)
      this.userService.reactivate(tenant, user, eventInfo); 
    Map<String, Object> map = Map.of("auth_time", Long.valueOf(ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()), "grants", 
        List.of(GrantType.authorization_code), "source", "oauth");
    RefreshToken.MetaData metaData = new RefreshToken.MetaData();
    metaData.device.lastAccessedAddress = this.frontEndSupport.getTrustedClientIPAddress();
    RefreshToken refreshToken = this.refreshTokenService.createRefreshToken(null, tenant, user, application, map, metaData);
    this.frontEndSupport.userLoginSecurityContext.login(new Tokens(null, refreshToken.token));
  }
}
