package io.fusionauth.api.service.useraction;

import com.google.inject.Inject;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.LockType;
import io.fusionauth.api.l10n.Localizer;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.event.WebhookTransactionException;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.lock.LockService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.util.EmailTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.LogHistory;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.UserActionOption;
import io.fusionauth.domain.UserActionReason;
import io.fusionauth.domain.api.user.ActionRequest;
import io.fusionauth.domain.email.Email;
import io.fusionauth.domain.event.UserActionEvent;
import io.fusionauth.domain.event.UserActionPhase;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mybatis.guice.transactional.Transactional;
import org.primeframework.email.domain.PreviewResult;
import org.primeframework.email.service.EmailService;
import org.primeframework.email.service.PreviewEmailBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultActionService implements ActionService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultActionService.class);
  
  private final ApplicationCache applicationCache;
  
  private final ApplicationReaderService applicationReader;
  
  private final EmailProxy emailProxy;
  
  private final EmailService emailService;
  
  private final ExternalIdentifierService externalIdentifierService;
  
  private final LockService lockService;
  
  private final RefreshTokenService refreshTokenService;
  
  private final TenantCache tenantCache;
  
  private final UserActionLogService userActionLogService;
  
  private final UserActionReasonService userActionReasonService;
  
  private final UserActionService userActionService;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultActionService(ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, EmailProxy paramEmailProxy, EmailService paramEmailService, ExternalIdentifierService paramExternalIdentifierService, LockService paramLockService, RefreshTokenService paramRefreshTokenService, TenantCache paramTenantCache, UserActionLogService paramUserActionLogService, UserActionReasonService paramUserActionReasonService, UserActionService paramUserActionService, UserReaderService paramUserReaderService) {
    this.applicationCache = paramApplicationCache;
    this.applicationReader = paramApplicationReaderService;
    this.emailProxy = paramEmailProxy;
    this.emailService = paramEmailService;
    this.externalIdentifierService = paramExternalIdentifierService;
    this.lockService = paramLockService;
    this.refreshTokenService = paramRefreshTokenService;
    this.tenantCache = paramTenantCache;
    this.userActionLogService = paramUserActionLogService;
    this.userActionReasonService = paramUserActionReasonService;
    this.userActionService = paramUserActionService;
    this.userReader = paramUserReaderService;
  }
  
  @Transactional
  public UserActionLog actionUser(Tenant paramTenant1, Tenant paramTenant2, User paramUser, UserAction paramUserAction, UserActionReason paramUserActionReason, ActionRequest.ActionData paramActionData, boolean paramBoolean, EventInfo paramEventInfo) throws UserAlreadyActionedException {
    String str1;
    handleDuplicateActions(paramActionData);
    UserActionOption userActionOption = (paramActionData.option != null) ? paramUserAction.getOption(paramActionData.option) : null;
    List<Locale> list = paramUser.preferredLanguages;
    UserActionLog userActionLog = new UserActionLog(paramActionData.actioneeUserId, paramActionData.actionerUserId, paramUserAction.id, paramActionData.applicationIds, paramActionData.comment, paramActionData.expiry, paramUserAction.name, Localizer.localize(list, paramUserAction.localizedNames, paramUserAction.name), paramActionData.option, Localizer.localize(list, (userActionOption != null) ? userActionOption.localizedNames : null, (userActionOption != null) ? userActionOption.name : null), (paramUserActionReason != null) ? paramUserActionReason.text : null, Localizer.localize(list, (paramUserActionReason != null) ? paramUserActionReason.localizedTexts : null, (paramUserActionReason != null) ? paramUserActionReason.text : null), (paramUserActionReason != null) ? paramUserActionReason.code : null, ZonedDateTime.now(ZoneOffset.UTC), (paramActionData.expiry != null && paramUserAction.sendEndEvent) ? Boolean.valueOf(false) : null, null, paramActionData.notifyUser, paramActionData.emailUser);
    this.userActionLogService.log(userActionLog);
    if (paramUserAction.preventLogin) {
      this.externalIdentifierService.deleteByUserId(paramUser.id, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.LoginIntent });
      if (paramTenant2.jwtConfiguration.refreshTokenRevocationPolicy.onLoginPrevented)
        this.refreshTokenService.revokeRefreshTokensByUser(paramTenant2, paramUser, paramEventInfo); 
    } 
    if (UserActionEvent.Infinite.equals(paramActionData.expiry)) {
      str1 = null;
    } else {
      str1 = paramUserAction.temporal ? Localizer.localize(list, Duration.between(ZonedDateTime.now(), userActionLog.expiry)) : null;
    } 
    String str2 = Localizer.localize(list, paramUserAction.localizedNames, paramUserAction.name);
    UserActionEvent userActionEvent = new UserActionEvent(paramEventInfo, userActionLog.id, userActionLog.actioneeUserId, userActionLog.actionerUserId, userActionLog.applicationIds, paramUserAction.name, str2, userActionLog.option, userActionLog.localizedOption, userActionLog.reason, userActionLog.localizedReason, userActionLog.reasonCode, userActionLog.expiry, str1, paramUserAction.temporal ? UserActionPhase.start : null, userActionLog.comment, paramActionData.notifyUser, paramActionData.emailUser, null);
    handleBroadcastAndEmailing(paramTenant1, paramUser, userActionLog, paramActionData, paramBoolean, userActionEvent, UserActionPhase.start);
    return userActionLog;
  }
  
  @Transactional
  public UserActionLog cancelAction(Tenant paramTenant, User paramUser, UserAction paramUserAction, UserActionLog paramUserActionLog, ActionRequest.ActionData paramActionData, boolean paramBoolean, EventInfo paramEventInfo) {
    if (paramUserActionLog.history == null)
      paramUserActionLog.history = new LogHistory(); 
    paramUserActionLog.history.add(paramUserActionLog.actionerUserId, paramUserActionLog.comment, paramUserActionLog.insertInstant, paramUserActionLog.expiry);
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    paramUserActionLog.comment = paramActionData.comment;
    paramUserActionLog.actionerUserId = paramActionData.actionerUserId;
    paramUserActionLog.insertInstant = zonedDateTime;
    paramUserActionLog.expiry = zonedDateTime;
    paramUserActionLog.notifyUserOnEnd = paramActionData.notifyUser;
    paramUserActionLog.emailUserOnEnd = paramActionData.emailUser;
    this.userActionLogService.update(paramUserActionLog);
    List<Locale> list = paramUser.preferredLanguages;
    String str = Localizer.localize(list, paramUserAction.localizedNames, paramUserAction.name);
    UserActionEvent userActionEvent = new UserActionEvent(paramEventInfo, paramUserActionLog.id, paramUserActionLog.actioneeUserId, paramActionData.actionerUserId, paramUserActionLog.applicationIds, paramUserAction.name, str, null, null, paramUserActionLog.reason, paramUserActionLog.localizedReason, paramUserActionLog.reasonCode, null, null, UserActionPhase.cancel, paramActionData.comment, paramActionData.notifyUser, paramActionData.emailUser, null);
    handleBroadcastAndEmailing(paramTenant, paramUser, paramUserActionLog, paramActionData, paramBoolean, userActionEvent, UserActionPhase.cancel);
    return paramUserActionLog;
  }
  
  @Transactional
  public void handleFromCleanSpeak(UUID paramUUID, ActionService.CleanSpeakUserAction paramCleanSpeakUserAction) {
    UserActionLog userActionLog;
    List<UUID> list;
    UserAction userAction = this.userActionService.retrieveByName(paramCleanSpeakUserAction.action);
    if (userAction == null || (userAction.temporal && paramCleanSpeakUserAction.expiry == null) || (!userAction.temporal && paramCleanSpeakUserAction.expiry != null))
      throw new CleanSpeakUserActionException(new Object[] { "Invalid action [" + paramCleanSpeakUserAction.action + "]. It is likely that the configuration between CleanSpeak and FusionAuth are not identical with respect to the action name and temporal flag." }); 
    if (!userAction.userEmailingEnabled && paramCleanSpeakUserAction.notifyUser)
      throw new CleanSpeakUserActionException(new Object[] { "Invalid action [" + paramCleanSpeakUserAction.action + "]. It is likely that the configuration between CleanSpeak and FusionAuth are not identical with respect to the action's emailing configuration CleanSpeak's action requesting to notify the user" }); 
    User user1 = this.userReader.retrieveById(paramUUID, paramCleanSpeakUserAction.userId);
    if (user1 == null)
      throw new CleanSpeakUserActionException(new Object[] { "Invalid userId [" + String.valueOf(paramCleanSpeakUserAction.userId) + "]" }); 
    User user2 = this.userReader.retrieveById(paramUUID, paramCleanSpeakUserAction.moderatorId);
    if (user2 == null)
      throw new CleanSpeakUserActionException(new Object[] { "Invalid moderatorId [" + String.valueOf(paramCleanSpeakUserAction.moderatorId) + "]" }); 
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    switch (paramCleanSpeakUserAction.phase) {
      case start:
        list = this.applicationReader.translateCleanSpeakApplicationIds(paramCleanSpeakUserAction.applicationIds);
        userActionLog = new UserActionLog(user1.id, user2.id, userAction.id, list, paramCleanSpeakUserAction.comment, paramCleanSpeakUserAction.expiry, paramCleanSpeakUserAction.action, paramCleanSpeakUserAction.localizedAction, paramCleanSpeakUserAction.key, paramCleanSpeakUserAction.localizedKey, paramCleanSpeakUserAction.reason, paramCleanSpeakUserAction.localizedReason, paramCleanSpeakUserAction.reasonCode, zonedDateTime, null, null, false, paramCleanSpeakUserAction.notifyUser);
        this.userActionLogService.log(userActionLog);
        break;
      case modify:
        userActionLog = this.userActionLogService.retrieveCurrent(paramCleanSpeakUserAction.userId, userAction.id);
        if (userActionLog == null)
          throw new CleanSpeakUserActionException(new Object[] { "User isn't in an action that can be modified" }); 
        if (userActionLog.history == null)
          userActionLog.history = new LogHistory(); 
        userActionLog.history.add(userActionLog.actionerUserId, userActionLog.comment, userActionLog.insertInstant, userActionLog.expiry);
        userActionLog.comment = paramCleanSpeakUserAction.comment;
        userActionLog.actionerUserId = user2.id;
        userActionLog.insertInstant = zonedDateTime;
        userActionLog.expiry = paramCleanSpeakUserAction.expiry;
        userActionLog.emailUserOnEnd = paramCleanSpeakUserAction.notifyUser;
        this.userActionLogService.update(userActionLog);
        break;
      case cancel:
      case end:
        userActionLog = this.userActionLogService.retrieveCurrent(paramCleanSpeakUserAction.userId, userAction.id);
        if (userActionLog == null)
          throw new CleanSpeakUserActionException(new Object[] { "User isn't in an action that can be ended" }); 
        if (userActionLog.history == null)
          userActionLog.history = new LogHistory(); 
        userActionLog.history.add(userActionLog.actionerUserId, userActionLog.comment, userActionLog.insertInstant, userActionLog.expiry);
        userActionLog.comment = paramCleanSpeakUserAction.comment;
        userActionLog.actionerUserId = user2.id;
        userActionLog.insertInstant = zonedDateTime;
        userActionLog.expiry = paramCleanSpeakUserAction.expiry;
        userActionLog.emailUserOnEnd = paramCleanSpeakUserAction.notifyUser;
        this.userActionLogService.update(userActionLog);
        break;
    } 
    if (paramCleanSpeakUserAction.notifyUser) {
      Tenant tenant = (Tenant)this.tenantCache.get(user1.tenantId);
      this.emailProxy.sendActionEmail(tenant, user1, paramCleanSpeakUserAction.phase, userAction, null);
    } 
  }
  
  public List<UserActionLog> retrieveAllForUser(User paramUser, Boolean paramBoolean) {
    List<UserActionLog> list = this.userActionLogService.retrieveAllForUser(paramUser.id);
    if (paramBoolean != null)
      list = (List<UserActionLog>)list.stream().filter(paramUserActionLog -> paramBoolean.equals(Boolean.valueOf(paramUserActionLog.isActive()))).collect(Collectors.toList()); 
    if (list != null && list.size() > 0)
      list.sort(Comparator.comparing(paramUserActionLog -> paramUserActionLog.insertInstant)); 
    return list;
  }
  
  public List<UserActionLog> retrieveAllForUserPreventingLogin(User paramUser) {
    List<UserActionLog> list = this.userActionService.retrieveAllCurrentPreventLoginActionLogsForUser(paramUser.id);
    if (list.isEmpty())
      return list; 
    list.sort(Comparator.comparing(paramUserActionLog -> paramUserActionLog.insertInstant));
    return list;
  }
  
  public UserActionLog retrieveById(UUID paramUUID) {
    return this.userActionLogService.retrieveById(paramUUID);
  }
  
  @Transactional
  public void sendEndEvents(EventInfo paramEventInfo) {
    this.lockService.acquireLock(LockType.UserActionEndEvent);
    List<UserAction> list = this.userActionService.retrieveAll();
    Map map = (Map)list.stream().collect(Collectors.toMap(paramUserAction -> paramUserAction.id, paramUserAction -> paramUserAction));
    List<UserActionLog> list1 = this.userActionLogService.retrieveExpired();
    for (UserActionLog userActionLog : list1) {
      try {
        UserAction userAction = (UserAction)map.get(userActionLog.userActionId);
        User user = this.userReader.retrieveById(null, userActionLog.actioneeUserId);
        String str = Localizer.localize(user.preferredLanguages, userAction.localizedNames, userAction.name);
        UserActionEvent userActionEvent = new UserActionEvent(paramEventInfo, userActionLog.id, userActionLog.actioneeUserId, userActionLog.actionerUserId, userActionLog.applicationIds, userAction.name, str, userActionLog.option, userActionLog.localizedOption, userActionLog.reason, userActionLog.localizedReason, userActionLog.reasonCode, userActionLog.expiry, null, UserActionPhase.end, userActionLog.comment, userActionLog.notifyUserOnEnd, userActionLog.emailUserOnEnd, null);
        if (userActionLog.notifyUserOnEnd && userAction.includeEmailInEventJSON)
          userActionEvent.email = generateEmail(user, UserActionPhase.end, userAction, userActionEvent); 
        Tenant tenant = (Tenant)this.tenantCache.get(user.tenantId);
        EventHelper.send(tenant, null, userActionEvent);
        this.userActionLogService.markEndEventSent(userActionLog);
        if (userActionLog.emailUserOnEnd)
          this.emailProxy.sendActionEmail(tenant, user, UserActionPhase.end, userAction, userActionEvent); 
      } catch (WebhookTransactionException webhookTransactionException) {
        logger.error("User Action End Event error", (Throwable)webhookTransactionException);
      } 
    } 
  }
  
  @Transactional
  public UserActionLog updateAction(Tenant paramTenant, User paramUser, UserAction paramUserAction, UserActionLog paramUserActionLog, ActionRequest.ActionData paramActionData, boolean paramBoolean, EventInfo paramEventInfo) throws UserAlreadyActionedException {
    String str1;
    if (paramUserActionLog.history == null)
      paramUserActionLog.history = new LogHistory(); 
    paramUserActionLog.history.add(paramUserActionLog.actionerUserId, paramUserActionLog.comment, paramUserActionLog.insertInstant, paramUserActionLog.expiry);
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    paramUserActionLog.comment = paramActionData.comment;
    paramUserActionLog.actionerUserId = paramActionData.actionerUserId;
    paramUserActionLog.insertInstant = zonedDateTime;
    paramUserActionLog.expiry = paramActionData.expiry;
    paramUserActionLog.emailUserOnEnd = paramActionData.emailUser;
    paramUserActionLog.notifyUserOnEnd = paramActionData.notifyUser;
    this.userActionLogService.update(paramUserActionLog);
    if (UserActionEvent.Infinite.equals(paramActionData.expiry)) {
      str1 = null;
    } else {
      str1 = paramUserAction.temporal ? Localizer.localize(paramUser.preferredLanguages, Duration.between(ZonedDateTime.now(ZoneOffset.UTC), paramUserActionLog.expiry)) : null;
    } 
    String str2 = Localizer.localize(paramUser.preferredLanguages, paramUserAction.localizedNames, paramUserAction.name);
    UserActionEvent userActionEvent = new UserActionEvent(paramEventInfo, paramUserActionLog.id, paramUserActionLog.actioneeUserId, paramActionData.actionerUserId, paramUserActionLog.applicationIds, paramUserAction.name, str2, null, null, paramUserActionLog.reason, paramUserActionLog.localizedReason, paramUserActionLog.reasonCode, paramActionData.expiry, str1, UserActionPhase.modify, paramActionData.comment, paramActionData.notifyUser, paramActionData.emailUser, null);
    handleBroadcastAndEmailing(paramTenant, paramUser, paramUserActionLog, paramActionData, paramBoolean, userActionEvent, UserActionPhase.modify);
    return paramUserActionLog;
  }
  
  public ActionService.ValidationResult validate(UUID paramUUID, ActionRequest.ActionData paramActionData) {
    ActionService.ValidationResult validationResult = new ActionService.ValidationResult();
    if (paramActionData.actioneeUserId != null) {
      validationResult.actioneeUser = this.userReader.retrieveById(paramUUID, paramActionData.actioneeUserId);
      if (validationResult.actioneeUser != null)
        validationResult.actioneeTenant = (Tenant)this.tenantCache.get(validationResult.actioneeUser.tenantId); 
    } 
    if (paramActionData.actionerUserId != null) {
      validationResult.actionerUser = this.userReader.retrieveById(paramUUID, paramActionData.actionerUserId);
      if (validationResult.actionerUser != null)
        validationResult.actionerTenant = (Tenant)this.tenantCache.get(validationResult.actionerUser.tenantId); 
    } 
    if (paramActionData.reasonId != null)
      validationResult.reason = this.userActionReasonService.retrieveById(paramActionData.reasonId); 
    if (paramActionData.userActionId != null)
      validationResult.action = this.userActionService.retrieveById(paramActionData.userActionId); 
    Validator validator = (new Validator()).notMissing(paramActionData.userActionId, "action.userActionId", new Object[0]).notMissing(paramActionData.actionerUserId, "action.actionerUserId", new Object[0]).notMissing(paramActionData.actioneeUserId, "action.actioneeUserId", new Object[0]).ifTrue((paramActionData.reasonId != null), paramValidator -> paramValidator.validObject(paramValidationResult.reason, "action.reasonId", new Object[] { paramActionData.reasonId })).ifTrue((paramActionData.actionerUserId != null), paramValidator -> paramValidator.validObject(paramValidationResult.actionerUser, "action.actionerUserId", new Object[] { paramActionData.actionerUserId })).ifTrue((paramActionData.actioneeUserId != null), paramValidator -> paramValidator.validObject(paramValidationResult.actioneeUser, "action.actioneeUserId", new Object[] { paramActionData.actioneeUserId }));
    if (paramActionData.applicationIds != null && paramActionData.applicationIds.size() > 0) {
      Set set = (Set)this.applicationCache.getAll().stream().map(paramApplication -> paramApplication.id).collect(Collectors.toSet());
      validator.forEach(paramActionData.applicationIds, (paramValidator, paramUUID, paramInteger) -> paramValidator.valid(paramSet.contains(paramUUID), "action.applicationIds", new Object[] { paramUUID }));
    } 
    if (paramActionData.userActionId != null) {
      validator.validObject(validationResult.action, "action.userActionId", new Object[] { paramActionData.userActionId });
      if (validationResult.action != null) {
        if (!validationResult.action.temporal && paramActionData.option != null) {
          UserActionOption userActionOption = validationResult.action.getOption(paramActionData.option);
          validator.validObject(userActionOption, "action.option", new Object[] { paramActionData.option });
        } else if (!validationResult.action.temporal && validationResult.action.options.size() > 0) {
          validator.notBlank(paramActionData.option, "action.option", new Object[0]);
        } 
        validator.ifTrue(validationResult.action.temporal, paramValidator -> paramValidator.notMissing(paramActionData.expiry, "action.expiry", new Object[0]).missing(paramActionData.option, "action.option", new Object[0]));
        validator.ifTrue(!validationResult.action.temporal, paramValidator -> paramValidator.missing(paramActionData.expiry, "action.expiry", new Object[0]));
        validator.ifTrue((validationResult.action.temporal && paramActionData.expiry != null), paramValidator -> paramValidator.ensure(paramActionData.expiry.isAfter(ZonedDateTime.now(ZoneOffset.UTC)), "action.expiry", "[inPast]", new Object[] { paramActionData.expiry }));
        validator.ifTrue(!validationResult.action.userNotificationsEnabled, paramValidator -> paramValidator.ensure(!paramActionData.notifyUser, "action.notifyUser", "[disabled]", new Object[0]));
      } 
    } 
    if (validationResult.action != null && paramActionData.emailUser) {
      UUID uUID = validationResult.action.startEmailTemplateId;
      validator.ensure(validationResult.action.userEmailingEnabled, "action.emailUser", "[emailingNotSupported]", new Object[0])
        .ensure((uUID != null), "action.emailUser", "[actionIsMissingTemplateDefinition]", new Object[0]);
    } 
    validationResult.errors = validator.done();
    return validationResult;
  }
  
  public ActionService.ValidationResult validateRetrieve(UUID paramUUID1, UUID paramUUID2) {
    ActionService.ValidationResult validationResult = new ActionService.ValidationResult();
    validationResult.actioneeUser = (paramUUID2 != null) ? this.userReader.retrieveById(null, paramUUID2) : null;
    if (validationResult.actioneeUser != null)
      validationResult.actioneeTenant = (Tenant)this.tenantCache.get(validationResult.actioneeUser.tenantId); 
    validationResult.log = (paramUUID1 != null) ? this.userActionLogService.retrieveById(paramUUID1) : null;
    validationResult



      
      .errors = (new Validator()).ensure((paramUUID1 != null || paramUUID2 != null), "actionId", "[missing]", new Object[0]).ifTrue((paramUUID2 != null), paramValidator -> paramValidator.validObject(paramValidationResult.actioneeUser, "userId", new Object[0])).done();
    return validationResult;
  }
  
  public ActionService.ValidationResult validateUpdateOrEnd(UUID paramUUID1, UUID paramUUID2, ActionRequest.ActionData paramActionData, boolean paramBoolean) {
    ActionService.ValidationResult validationResult = new ActionService.ValidationResult();
    Validator validator = (new Validator()).notMissing(paramUUID2, "actionId", new Object[0]);
    if (paramUUID2 != null) {
      validationResult.log = this.userActionLogService.retrieveById(paramUUID2);
      if (validationResult.log == null || validationResult.log.expiry == null || validationResult.log.expiry.isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
        validationResult.log = null;
        return validationResult;
      } 
      validationResult.actioneeUser = this.userReader.retrieveById(paramUUID1, validationResult.log.actioneeUserId);
      if (validationResult.actioneeUser != null)
        validationResult.actioneeTenant = (Tenant)this.tenantCache.get(validationResult.actioneeUser.tenantId); 
      validationResult.action = this.userActionService.retrieveById(validationResult.log.userActionId);
    } 
    if (paramActionData.actionerUserId != null) {
      validationResult.actionerUser = this.userReader.retrieveById(paramUUID1, paramActionData.actionerUserId);
      if (validationResult.actionerUser != null)
        validationResult.actionerTenant = (Tenant)this.tenantCache.get(validationResult.actionerUser.tenantId); 
    } 
    validator.notMissing(paramActionData.actionerUserId, "action.actionerUserId", new Object[0])
      .ifTrue((paramActionData.actionerUserId != null), paramValidator -> paramValidator.validObject(paramValidationResult.actionerUser, "action.actionerUserId", new Object[] { paramActionData.actionerUserId })).ifTrue((validationResult.log.actioneeUserId != null), paramValidator -> paramValidator.validObject(paramValidationResult.actioneeUser, "action.actioneeUserId", new Object[] { paramValidationResult.log.actioneeUserId })).ifTrue(paramBoolean, paramValidator -> paramValidator.notMissing(paramActionData.expiry, "action.expiry", new Object[0]))
      .ifTrue((paramBoolean && paramActionData.expiry != null), paramValidator -> paramValidator.ensure(paramActionData.expiry.isAfter(ZonedDateTime.now()), "action.expiry", "[inPast]", new Object[] { paramActionData.expiry }));
    if (validationResult.action != null) {
      validator.ifTrue(!validationResult.action.userNotificationsEnabled, paramValidator -> paramValidator.ensure(!paramActionData.notifyUser, "action.notifyUser", "[disabled]", new Object[0]));
      if (paramActionData.emailUser) {
        UUID uUID = paramBoolean ? validationResult.action.modifyEmailTemplateId : validationResult.action.cancelEmailTemplateId;
        validator.ensure(validationResult.action.userEmailingEnabled, "action.emailUser", "[emailingNotSupported]", new Object[0])
          .ensure((uUID != null), "action.emailUser", "[actionIsMissingTemplateDefinition]", new Object[0]);
      } 
    } 
    validationResult.errors = validator.done();
    return validationResult;
  }
  
  private Email generateEmail(User paramUser, UserActionPhase paramUserActionPhase, UserAction paramUserAction, UserActionEvent paramUserActionEvent) {
    new PreviewResult();
    switch (paramUserActionPhase) {
      default:
        throw new MatchException(null, null);
      case start:
      
      case modify:
      
      case cancel:
      
      case end:
        break;
    } 
    PreviewResult previewResult = 


      
      getPreviewResult(paramUser, paramUserAction.endEmailTemplateId, paramUserActionEvent);
    if (previewResult.wasSuccessful())
      return EmailTools.convert(previewResult.email); 
    return null;
  }
  
  private PreviewResult getPreviewResult(User paramUser, UUID paramUUID, UserActionEvent paramUserActionEvent) {
    if (paramUserActionEvent == null)
      paramUserActionEvent = new UserActionEvent(); 
    return ((PreviewEmailBuilder)((PreviewEmailBuilder)((PreviewEmailBuilder)this.emailService.preview(null, paramUUID, paramUser.preferredLanguages)
      .withTemplateParameter("user", (new User(paramUser)).secure()))
      .withTemplateParameter("event", paramUserActionEvent))
      .to(new String[] { paramUser.email })).go();
  }
  
  private void handleBroadcastAndEmailing(Tenant paramTenant, User paramUser, UserActionLog paramUserActionLog, ActionRequest.ActionData paramActionData, boolean paramBoolean, UserActionEvent paramUserActionEvent, UserActionPhase paramUserActionPhase) {
    assert paramUserActionPhase != UserActionPhase.end;
    UserAction userAction = null;
    if (paramBoolean) {
      userAction = this.userActionService.retrieveById(paramUserActionLog.userActionId);
      if (paramActionData.notifyUser && userAction.includeEmailInEventJSON)
        paramUserActionEvent.email = generateEmail(paramUser, paramUserActionPhase, userAction, paramUserActionEvent); 
      EventHelper.send(paramTenant, null, paramUserActionEvent);
      if (UserActionPhase.cancel == paramUserActionPhase)
        this.userActionLogService.markEndEventSent(paramUserActionLog); 
    } 
    if (paramActionData.emailUser && paramUser.email != null) {
      if (userAction == null)
        userAction = this.userActionService.retrieveById(paramUserActionLog.userActionId); 
      this.emailProxy.sendActionEmail(paramTenant, paramUser, paramUserActionPhase, userAction, paramUserActionEvent);
    } 
  }
  
  private void handleDuplicateActions(ActionRequest.ActionData paramActionData) {
    UserActionLog userActionLog = this.userActionLogService.retrieveCurrent(paramActionData.actioneeUserId, paramActionData.userActionId);
    if (userActionLog != null)
      throw new UserAlreadyActionedException(); 
  }
}
