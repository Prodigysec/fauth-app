package io.fusionauth.api.service.connector;

import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.group.GroupReaderService;
import io.fusionauth.api.service.group.GroupService;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.api.service.system.eventLog.ErrorLog;
import io.fusionauth.api.service.user.DefaultUserService;
import io.fusionauth.api.service.user.IdentityHelper;
import io.fusionauth.api.service.user.PasswordService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.ChangePasswordReason;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.IdentityVerifiedReason;
import io.fusionauth.domain.PasswordType;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorPolicy;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.mybatis.guice.transactional.Transactional;

public abstract class BaseExternalConnector extends BaseConnector implements ExternalConnector {
  protected final ApplicationCache applicationCache;
  
  protected final ApplicationReaderService applicationReader;
  
  protected final GroupReaderService groupReader;
  
  protected final GroupService groupService;
  
  protected final UserReaderService userReader;
  
  protected BaseExternalConnector(EmailProxy paramEmailProxy, PasswordService paramPasswordService, ReactorService paramReactorService, UserMapper paramUserMapper, UserService paramUserService, ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, GroupReaderService paramGroupReaderService, GroupService paramGroupService, UserReaderService paramUserReaderService) {
    super(paramEmailProxy, paramPasswordService, paramReactorService, paramUserMapper, paramUserService);
    this.applicationCache = paramApplicationCache;
    this.applicationReader = paramApplicationReaderService;
    this.groupReader = paramGroupReaderService;
    this.groupService = paramGroupService;
    this.userReader = paramUserReaderService;
  }
  
  @Transactional
  public AuthenticationService.AuthenticationResult synchronizeExternalUser(Tenant paramTenant, Application paramApplication, AuthenticationService.AuthenticationResult paramAuthenticationResult, String paramString1, List<IdentityType> paramList, String paramString2, EventInfo paramEventInfo) {
    Objects.requireNonNull(paramAuthenticationResult);
    Debugger debugger = paramAuthenticationResult.debugger;
    try {
      paramAuthenticationResult.user.normalize();
      UUID uUID = paramAuthenticationResult.user.id;
      ConnectorPolicy connectorPolicy = paramAuthenticationResult.connectorPolicy;
      User user1 = (new User(paramAuthenticationResult.user)).with(paramUser -> paramUser.password = paramString).with(paramUser -> paramUser.connectorId = paramConnectorPolicy.migrate ? BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID : paramConnectorPolicy.connectorId);
      user1.normalize();
      User user2 = this.userReader.retrieveById(paramTenant.id, uUID);
      List<? extends GroupMember> list = user1.getMemberships().stream().map(GroupMember::new).toList();
      List<UserRegistration> list1 = (List)user1.getRegistrations().stream().map(UserRegistration::new).collect(Collectors.toList());
      if (user2 == null) {
        debugger.log("User with Id [" + String.valueOf(uUID) + "] does not exist in tenant [" + String.valueOf(paramTenant.id) + "]. Create the user.");
        HashMap<Object, Object> hashMap = new HashMap<>();
        DefaultUserService.handleUserPrimaryIdentities(user1, (Map)hashMap);
        user1.identities.forEach(paramUserIdentity -> paramUserIdentity.verifiedReason = IdentityVerifiedReason.Trusted);
        boolean bool = validateUser(() -> this.userService.validateConnectorCreate(paramTenant, paramUser, paramApplication, paramEventInfo, true), paramAuthenticationResult.connectorConfiguration, paramTenant, user1);
        if (!bool)
          return null; 
        UserService.UserResult userResult = this.userService.create(paramTenant, null, user1, SendSetPasswordIdentityType.doNotSend, false, false, false, false, paramEventInfo, null);
        paramAuthenticationResult.user.phoneNumber = userResult.user.phoneNumber;
        createRegistrationsBasedUponMigrationPolicy(paramTenant, user1, list1, paramEventInfo);
      } else {
        debugger.log("User with Id [" + String.valueOf(uUID) + "] already exists in tenant [" + String.valueOf(paramTenant.id) + "]. Synchronize the user.");
        user1.id = user2.id;
        for (UserRegistration userRegistration : user2.getRegistrations())
          this.userService.deleteRegistration(paramTenant, userRegistration, user2, paramApplication, paramEventInfo); 
        this.groupService.removeUserMemberships(paramTenant, user2, paramEventInfo);
        boolean bool = validateUser(() -> this.userService.validateConnectorUpdate(paramTenant, paramApplication, paramEventInfo, paramUser), paramAuthenticationResult.connectorConfiguration, paramTenant, user1);
        if (!bool)
          return null; 
        UserService.UpdateUserOptions updateUserOptions = (new UserService.UpdateUserOptions()).with(paramUpdateUserOptions -> paramUpdateUserOptions.deleteRefreshTokensOnPasswordChange = false).with(paramUpdateUserOptions -> paramUpdateUserOptions.sendPasswordUpdatedEventOnPasswordChange = false).with(paramUpdateUserOptions -> paramUpdateUserOptions.skipVerification = false);
        this.userService.updateAllowConnectorIdChange(paramTenant, paramApplication, user2, user1, updateUserOptions, paramEventInfo, PasswordType.PLAINTEXT);
        createRegistrationsBasedUponMigrationPolicy(paramTenant, user1, list1, paramEventInfo);
        for (GroupMember groupMember : list) {
          groupMember.userId = user2.id;
          Group group = this.groupReader.retrieveById(paramTenant.id, groupMember.groupId);
          this.groupService.addMembers(Map.of(paramTenant.id, paramTenant), 
              Map.of(groupMember.groupId, group), 
              Map.of(groupMember.groupId, Collections.singletonList(groupMember)), 
              Map.of(user2.id, user2), true, paramEventInfo);
        } 
      } 
      paramAuthenticationResult.user.getMemberships().clear();
      paramAuthenticationResult.user.getMemberships().addAll(list);
      paramAuthenticationResult.user.getRegistrations().clear();
      paramAuthenticationResult.user.getRegistrations().addAll(list1);
      paramAuthenticationResult.user.tenantId = paramTenant.id;
      paramAuthenticationResult.user.connectorId = user1.connectorId;
      paramAuthenticationResult.user.id = user1.id;
      paramAuthenticationResult.user.insertInstant = user1.insertInstant;
      paramAuthenticationResult.user.lastLoginInstant = user1.lastLoginInstant;
      paramAuthenticationResult.user.lastUpdateInstant = user1.lastUpdateInstant;
      paramAuthenticationResult.user.verified = user1.verified;
      paramAuthenticationResult.user.verifiedInstant = user1.verifiedInstant;
      paramAuthenticationResult.user.uniqueUsername = user1.uniqueUsername;
      paramAuthenticationResult.user.identities.clear();
      paramAuthenticationResult.user.identities.addAll(user1.identities);
      paramAuthenticationResult.userIdentity = IdentityHelper.resolveIdentity(paramAuthenticationResult.user, paramString1, paramList);
      if (paramAuthenticationResult.userIdentity == null)
        paramAuthenticationResult.userIdentity = paramAuthenticationResult.user.resolveFirstIdentity(); 
      if (paramAuthenticationResult.userIdentity == null)
        throw new Exception("The user identity was not found after synchronization."); 
      paramAuthenticationResult.user.normalize();
      if (connectorPolicy.migrate) {
        debugger.log("Policy indicates the user will be migrated.");
        paramAuthenticationResult.user.passwordLastUpdateInstant = user1.passwordLastUpdateInstant;
        if (paramAuthenticationResult.user.passwordChangeRequired && paramAuthenticationResult.user.passwordChangeReason == null)
          paramAuthenticationResult.user.passwordChangeReason = ChangePasswordReason.Administrative; 
        checkIfPasswordChangeIsRequiredAndUpdateUser(paramTenant, paramApplication, paramString2, paramAuthenticationResult, paramEventInfo);
      } else {
        paramAuthenticationResult.user.passwordChangeRequired = false;
        paramAuthenticationResult.user.passwordChangeReason = null;
        paramAuthenticationResult.user.passwordLastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
      } 
    } catch (Exception exception) {
      debugger.log("Failed to synchronize the user. See error event log.")
        .done();
      handleException(exception, "Failed to synchronize the user.");
      paramAuthenticationResult = null;
    } finally {
      debugger.done();
    } 
    return paramAuthenticationResult;
  }
  
  protected boolean canHandle(ConnectorPolicy paramConnectorPolicy, String paramString) {
    if (paramConnectorPolicy.domains.contains("*"))
      return true; 
    int i = paramString.indexOf('@');
    if (i != -1) {
      String str = paramString.substring(i + 1).toLowerCase();
      return paramConnectorPolicy.domains.contains(str);
    } 
    return false;
  }
  
  protected void handleException(Exception paramException, String paramString) {
    if (paramString == null)
      paramString = ""; 
    String str1 = paramException.getMessage();
    String str2 = paramString + "\nException encountered.\n\n" + paramString + " : Message: " + paramException.getClass().getCanonicalName();
    Throwable throwable = paramException.getCause();
    if (throwable != null) {
      String str = throwable.getMessage();
      str2 = str2 + "\nCause\n\n" + str2 + " : Message: " + throwable.getClass().getCanonicalName();
    } 
    StringWriter stringWriter = new StringWriter();
    paramException.printStackTrace(new PrintWriter(stringWriter));
    str2 = str2 + "\n\n" + str2;
    EventLogHelper.create(new EventLog(EventLogType.Error, str2));
  }
  
  private void createRegistrationsBasedUponMigrationPolicy(Tenant paramTenant, User paramUser, List<UserRegistration> paramList, EventInfo paramEventInfo) {
    for (UserRegistration userRegistration : paramList) {
      Objects.requireNonNull(this.applicationReader);
      Application application = this.applicationCache.get(paramTenant.id, userRegistration.applicationId, this.applicationReader::retrieveById);
      List<ApplicationRole> list = (List)userRegistration.roles.stream().map(paramString -> {
            Objects.requireNonNull(this.applicationReader);
            return this.applicationCache.getRoleByName(paramTenant.id, paramUserRegistration.applicationId, paramString, this.applicationReader::retrieveById);
          }).collect(Collectors.toList());
      this.userService.createRegistration(paramTenant, application, paramUser, userRegistration, list, false, true, false, paramEventInfo);
    } 
  }
  
  private boolean validateUser(Supplier<UserService.ValidationResult> paramSupplier, BaseConnectorConfiguration paramBaseConnectorConfiguration, Tenant paramTenant, User paramUser) {
    UserService.ValidationResult validationResult = paramSupplier.get();
    if (!validationResult.errors.empty()) {
      (new ErrorLog("The user returned from connector [" + paramBaseConnectorConfiguration.name + "] failed validation when importing into the FusionAuth database."))
        .log("Validation errors:")
        .logObjectToJSON(validationResult.errors)
        .log("User:")
        .logObjectToJSON((new User(paramUser)).secure().sort())
        .done();
      return false;
    } 
    for (UserRegistration userRegistration : paramUser.getRegistrations()) {
      validationResult = this.userService.validateRegistrationCreate(paramTenant, userRegistration, null, true, false);
      if (!validationResult.errors.empty()) {
        (new ErrorLog("The user returned from connector [" + paramBaseConnectorConfiguration.name + "] failed validation when importing into the FusionAuth database."))
          .log("Validation errors:")
          .logObjectToJSON(validationResult.errors)
          .log("User:")
          .logObjectToJSON((new User(paramUser)).secure().sort())
          .done();
        return false;
      } 
    } 
    return true;
  }
}
