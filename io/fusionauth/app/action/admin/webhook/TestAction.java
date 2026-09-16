package io.fusionauth.app.action.admin.webhook;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.time.DurationFormatter;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.domain.guice.FusionAuthTenantId;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.event.EventSenderResult;
import io.fusionauth.api.service.event.EventService;
import io.fusionauth.api.service.event.WebhookEventSender;
import io.fusionauth.api.service.event.WebhookService;
import io.fusionauth.api.service.group.GroupReaderService;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.claims.JWTType;
import io.fusionauth.api.service.risk.RiskLevel;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.service.useraction.UserActionReasonService;
import io.fusionauth.api.util.TwoFactorTools;
import io.fusionauth.app.UserTestTools;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.AuthenticationThreats;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.EntityTypePermission;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.LocalizedStrings;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserActionOption;
import io.fusionauth.domain.UserActionReason;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.email.Email;
import io.fusionauth.domain.email.EmailAddress;
import io.fusionauth.domain.event.AuditLogCreateEvent;
import io.fusionauth.domain.event.BaseEvent;
import io.fusionauth.domain.event.EntityCreateCompleteEvent;
import io.fusionauth.domain.event.EntityCreateEvent;
import io.fusionauth.domain.event.EntityDeleteCompleteEvent;
import io.fusionauth.domain.event.EntityDeleteEvent;
import io.fusionauth.domain.event.EntityUpdateCompleteEvent;
import io.fusionauth.domain.event.EntityUpdateEvent;
import io.fusionauth.domain.event.EventLogCreateEvent;
import io.fusionauth.domain.event.EventRequest;
import io.fusionauth.domain.event.EventType;
import io.fusionauth.domain.event.GroupCreateCompleteEvent;
import io.fusionauth.domain.event.GroupCreateEvent;
import io.fusionauth.domain.event.GroupDeleteCompleteEvent;
import io.fusionauth.domain.event.GroupDeleteEvent;
import io.fusionauth.domain.event.GroupMemberAddCompleteEvent;
import io.fusionauth.domain.event.GroupMemberAddEvent;
import io.fusionauth.domain.event.GroupMemberRemoveCompleteEvent;
import io.fusionauth.domain.event.GroupMemberRemoveEvent;
import io.fusionauth.domain.event.GroupMemberUpdateCompleteEvent;
import io.fusionauth.domain.event.GroupMemberUpdateEvent;
import io.fusionauth.domain.event.GroupUpdateCompleteEvent;
import io.fusionauth.domain.event.GroupUpdateEvent;
import io.fusionauth.domain.event.JWTPublicKeyUpdateEvent;
import io.fusionauth.domain.event.JWTRefreshEvent;
import io.fusionauth.domain.event.JWTRefreshTokenRevokeEvent;
import io.fusionauth.domain.event.KickstartSuccessEvent;
import io.fusionauth.domain.event.UserActionEvent;
import io.fusionauth.domain.event.UserActionPhase;
import io.fusionauth.domain.event.UserBulkCreateEvent;
import io.fusionauth.domain.event.UserCreateCompleteEvent;
import io.fusionauth.domain.event.UserCreateEvent;
import io.fusionauth.domain.event.UserDeactivateEvent;
import io.fusionauth.domain.event.UserDeleteCompleteEvent;
import io.fusionauth.domain.event.UserDeleteEvent;
import io.fusionauth.domain.event.UserEmailUpdateEvent;
import io.fusionauth.domain.event.UserEmailVerifiedEvent;
import io.fusionauth.domain.event.UserIdentityProviderLinkEvent;
import io.fusionauth.domain.event.UserIdentityProviderUnlinkEvent;
import io.fusionauth.domain.event.UserIdentityUpdateEvent;
import io.fusionauth.domain.event.UserIdentityVerifiedEvent;
import io.fusionauth.domain.event.UserLoginFailedEvent;
import io.fusionauth.domain.event.UserLoginIdDuplicateOnCreateEvent;
import io.fusionauth.domain.event.UserLoginIdDuplicateOnUpdateEvent;
import io.fusionauth.domain.event.UserLoginNewDeviceEvent;
import io.fusionauth.domain.event.UserLoginSuccessEvent;
import io.fusionauth.domain.event.UserLoginSuspiciousEvent;
import io.fusionauth.domain.event.UserPasswordBreachEvent;
import io.fusionauth.domain.event.UserPasswordResetSendEvent;
import io.fusionauth.domain.event.UserPasswordResetStartEvent;
import io.fusionauth.domain.event.UserPasswordResetSuccessEvent;
import io.fusionauth.domain.event.UserPasswordUpdateEvent;
import io.fusionauth.domain.event.UserReactivateEvent;
import io.fusionauth.domain.event.UserRegistrationCreateCompleteEvent;
import io.fusionauth.domain.event.UserRegistrationCreateEvent;
import io.fusionauth.domain.event.UserRegistrationDeleteCompleteEvent;
import io.fusionauth.domain.event.UserRegistrationDeleteEvent;
import io.fusionauth.domain.event.UserRegistrationUpdateCompleteEvent;
import io.fusionauth.domain.event.UserRegistrationUpdateEvent;
import io.fusionauth.domain.event.UserRegistrationVerifiedEvent;
import io.fusionauth.domain.event.UserTwoFactorChallengeEvent;
import io.fusionauth.domain.event.UserTwoFactorFailedAttemptEvent;
import io.fusionauth.domain.event.UserTwoFactorMethodAddEvent;
import io.fusionauth.domain.event.UserTwoFactorMethodRemoveEvent;
import io.fusionauth.domain.event.UserTwoFactorSuccessEvent;
import io.fusionauth.domain.event.UserUpdateCompleteEvent;
import io.fusionauth.domain.event.UserUpdateEvent;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.scope.annotation.BrowserActionSession;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{id}", requiresAuthentication = true, constraints = {"admin", "webhook_manager"})
@Redirect(uri = "/admin/webhook/test/${id}")
public class TestAction extends BaseAction {
  @FTLVariable
  public static final List<EventType> eventTypes = EventType.allTypes();
  
  private final ApplicationReaderService applicationReader;
  
  private final EventService eventService;
  
  private final UUID fusionAuthTenantId;
  
  private final GroupReaderService groupReader;
  
  private final JWTService jwtService;
  
  private final KeyReaderService keyReaderService;
  
  private final MetricRegistry metricRegistry;
  
  private final ObjectMapper objectMapper;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  private final UserActionReasonService userActionReasonService;
  
  private final WebhookService webhookService;
  
  public String AuditLogCreateExample;
  
  public String EntityCreateCompleteExample;
  
  public String EntityCreateExample;
  
  public String EntityDeleteCompleteExample;
  
  public String EntityDeleteExample;
  
  public String EntityUpdateCompleteExample;
  
  public String EntityUpdateExample;
  
  public String EventLogCreateExample;
  
  public String GroupCreateCompleteExample;
  
  public String GroupCreateExample;
  
  public String GroupDeleteCompleteExample;
  
  public String GroupDeleteExample;
  
  public String GroupMemberAddCompleteExample;
  
  public String GroupMemberAddExample;
  
  public String GroupMemberRemoveCompleteExample;
  
  public String GroupMemberRemoveExample;
  
  public String GroupMemberUpdateCompleteExample;
  
  public String GroupMemberUpdateExample;
  
  public String GroupUpdateCompleteExample;
  
  public String GroupUpdateExample;
  
  public String JWTPublicKeyUpdateExample;
  
  public String JWTRefreshExample;
  
  public String JWTRefreshTokenRevokeExample;
  
  public String KickstartSuccessExample;
  
  public String UserAction_CodeExample;
  
  public String UserAction_TemporalExample;
  
  public String UserBulkCreateExample;
  
  public String UserCreateCompleteExample;
  
  public String UserCreateExample;
  
  public String UserDeactivateExample;
  
  public String UserDeleteCompleteExample;
  
  public String UserDeleteExample;
  
  public String UserEmailUpdateExample;
  
  public String UserEmailVerifiedExample;
  
  public String UserIdentityProviderLinkExample;
  
  public String UserIdentityProviderUnlinkExample;
  
  public String UserIdentityUpdateExample;
  
  public String UserIdentityVerifiedExample;
  
  public String UserLoginFailedExample;
  
  public String UserLoginIdDuplicateOnCreateExample;
  
  public String UserLoginIdDuplicateOnUpdateExample;
  
  public String UserLoginNewDeviceExample;
  
  public String UserLoginSuccessExample;
  
  public String UserLoginSuspiciousExample;
  
  public String UserPasswordBreachExample;
  
  public String UserPasswordResetSendExample;
  
  public String UserPasswordResetStartExample;
  
  public String UserPasswordResetSuccessExample;
  
  public String UserPasswordUpdateExample;
  
  public String UserReactivateExample;
  
  public String UserRegistrationCreateCompleteExample;
  
  public String UserRegistrationCreateExample;
  
  public String UserRegistrationDeleteCompleteExample;
  
  public String UserRegistrationDeleteExample;
  
  public String UserRegistrationUpdateCompleteExample;
  
  public String UserRegistrationUpdateExample;
  
  public String UserRegistrationVerifiedExample;
  
  public String UserTwoFactorChallengeExample;
  
  public String UserTwoFactorFailedAttemptExample;
  
  public String UserTwoFactorMethodAddExample;
  
  public String UserTwoFactorMethodRemoveExample;
  
  public String UserTwoFactorSuccessExample;
  
  public String UserUpdateCompleteExample;
  
  public String UserUpdateExample;
  
  @BrowserActionSession
  public String eventType;
  
  public UUID id;
  
  public EventType type;
  
  public Webhook webhook;
  
  private EventRequest eventRequest;
  
  private Field field;
  
  private String selectedEventType;
  
  @Inject
  public TestAction(FrontEndSupport paramFrontEndSupport, ApplicationReaderService paramApplicationReaderService, EventService paramEventService, @FusionAuthTenantId UUID paramUUID, GroupReaderService paramGroupReaderService, JWTService paramJWTService, KeyReaderService paramKeyReaderService, MetricRegistry paramMetricRegistry, ObjectMapper paramObjectMapper, ProxyInfoSupplier paramProxyInfoSupplier, UserActionReasonService paramUserActionReasonService, WebhookService paramWebhookService) {
    super(paramFrontEndSupport);
    this.applicationReader = paramApplicationReaderService;
    this.eventService = paramEventService;
    this.fusionAuthTenantId = paramUUID;
    this.groupReader = paramGroupReaderService;
    this.jwtService = paramJWTService;
    this.keyReaderService = paramKeyReaderService;
    this.metricRegistry = paramMetricRegistry;
    this.objectMapper = paramObjectMapper;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
    this.userActionReasonService = paramUserActionReasonService;
    this.webhookService = paramWebhookService;
  }
  
  public String get() {
    return "input";
  }
  
  @PostParameterMethod
  public void loadWebhook() {
    this.webhook = this.webhookService.retrieveById(this.id);
  }
  
  public String post() {
    Webhook webhook = (this.webhook.sslCertificateKeyId != null) ? (new Webhook(this.webhook)).with(paramWebhook -> paramWebhook.sslCertificate = ((KeyResponse)superDelegate().execute(())).key.certificate) : this.webhook;
    WebhookEventSender webhookEventSender = new WebhookEventSender(this.metricRegistry, this.proxyInfoSupplier, signingKey(webhook), webhook, null, this.objectMapper);
    webhookEventSender.logFailures = false;
    EventSenderResult eventSenderResult = this.eventService.sendTest(this.eventRequest.event, webhookEventSender);
    if (!eventSenderResult.success) {
      this.frontEndSupport.addGeneralError("[badResponse]", new Object[] { eventSenderResult.message });
      return "input";
    } 
    this.frontEndSupport.addGeneralInfo("[TestSuccess]", new Object[0]);
    return "success";
  }
  
  @PreRenderMethod
  public void preRenderSetup() {
    List<Application> list = this.applicationReader.retrieveAll(null, Collections.emptySet());
    Application application = list.get(0);
    User user = (new User(this.codeCurrentUser)).secure();
    List<Group> list1 = this.groupReader.retrieveAll(application.tenantId);
    Group group = (list1.size() > 0) ? list1.get(0) : (new Group()).with(paramGroup -> paramGroup.name = "Example group").with(paramGroup -> paramGroup.id = UUID.randomUUID());
    EntityType entityType = (new EntityType()).with(paramEntityType -> paramEntityType.id = UUID.randomUUID()).with(paramEntityType -> paramEntityType.name = "Example Entity Type").with(paramEntityType -> paramEntityType.permissions.add(new EntityTypePermission("read")));
    Entity entity = (new Entity()).with(paramEntity -> paramEntity.id = UUID.randomUUID()).with(paramEntity -> paramEntity.clientId = UUID.randomUUID().toString()).with(paramEntity -> paramEntity.name = "Example entity").with(paramEntity -> paramEntity.type = paramEntityType);
    EventInfo eventInfo = this.frontEndSupport.buildEventInfo(null);
    List<UserActionReason> list2 = this.userActionReasonService.retrieveAll();
    String str1 = list2.isEmpty() ? null : ((UserActionReason)list2.get(0)).text;
    String str2 = list2.isEmpty() ? null : ((UserActionReason)list2.get(0)).code;
    List<UUID> list3 = list.stream().map(paramApplication -> paramApplication.id).toList();
    Email email = (new Email()).with(paramEmail -> paramEmail.from = new EmailAddress("from@test.com", "Test from")).with(paramEmail -> paramEmail.html = "HTML body of the email").with(paramEmail -> paramEmail.subject = "Test user action email").with(paramEmail -> paramEmail.text = "Test body of the email").with(paramEmail -> paramEmail.to = Collections.singletonList(new EmailAddress("to@test.com", "Test to")));
    ZonedDateTime zonedDateTime1 = ZonedDateTime.now();
    ZonedDateTime zonedDateTime2 = zonedDateTime1.plusDays(2L);
    this.UserAction_TemporalExample = buildEvent((new UserActionEvent()).with(paramUserActionEvent -> paramUserActionEvent.actionId = UUID.randomUUID())
        .with(paramUserActionEvent -> paramUserActionEvent.actioneeUserId = UUID.randomUUID())
        .with(paramUserActionEvent -> paramUserActionEvent.actionerUserId = paramUser.id)
        .with(paramUserActionEvent -> paramUserActionEvent.applicationIds.addAll(paramList))
        .with(paramUserActionEvent -> paramUserActionEvent.action = "Example Action Name")
        .with(paramUserActionEvent -> paramUserActionEvent.localizedAction = "Example Localized Name")
        .with(paramUserActionEvent -> paramUserActionEvent.reason = paramString)
        .with(paramUserActionEvent -> paramUserActionEvent.localizedReason = paramString)
        .with(paramUserActionEvent -> paramUserActionEvent.reasonCode = paramString)
        .with(paramUserActionEvent -> paramUserActionEvent.expiry = paramZonedDateTime)
        .with(paramUserActionEvent -> paramUserActionEvent.localizedDuration = DurationFormatter.format(Duration.between(paramZonedDateTime1, paramZonedDateTime2), Locale.ENGLISH))
        .with(paramUserActionEvent -> paramUserActionEvent.phase = UserActionPhase.start)
        .with(paramUserActionEvent -> paramUserActionEvent.comment = "Test comment")
        .with(paramUserActionEvent -> paramUserActionEvent.notifyUser = true)
        .with(paramUserActionEvent -> paramUserActionEvent.email = paramEmail));
    UserAction userAction = (new UserAction()).with(paramUserAction -> paramUserAction.options.add(new UserActionOption("potato", new LocalizedStrings(Locale.FRENCH, "pomme de terre", Locale.GERMAN, "Kartoffel")))).with(paramUserAction -> paramUserAction.options.add(new UserActionOption("potato", new LocalizedStrings(Locale.FRENCH, "oignon", Locale.GERMAN, "Zwiebel"))));
    UserActionOption userActionOption = userAction.options.get(0);
    this.UserAction_CodeExample = buildEvent((new UserActionEvent()).with(paramUserActionEvent -> paramUserActionEvent.actionId = paramUserAction.id)
        .with(paramUserActionEvent -> paramUserActionEvent.actioneeUserId = UUID.randomUUID())
        .with(paramUserActionEvent -> paramUserActionEvent.actionerUserId = paramUser.id)
        .with(paramUserActionEvent -> paramUserActionEvent.applicationIds.addAll(paramList))
        .with(paramUserActionEvent -> paramUserActionEvent.action = paramUserAction.name)
        .with(paramUserActionEvent -> paramUserActionEvent.localizedAction = paramUserAction.name)
        .with(paramUserActionEvent -> paramUserActionEvent.reason = paramString)
        .with(paramUserActionEvent -> paramUserActionEvent.localizedReason = paramString)
        .with(paramUserActionEvent -> paramUserActionEvent.reasonCode = paramString)
        .with(paramUserActionEvent -> paramUserActionEvent.option = paramUserActionOption.name)
        .with(paramUserActionEvent -> paramUserActionEvent.localizedOption = paramUserActionOption.name)
        .with(paramUserActionEvent -> paramUserActionEvent.phase = UserActionPhase.start)
        .with(paramUserActionEvent -> paramUserActionEvent.comment = "Test comment")
        .with(paramUserActionEvent -> paramUserActionEvent.notifyUser = true)
        .with(paramUserActionEvent -> paramUserActionEvent.email = paramEmail));
    this.AuditLogCreateExample = buildEvent(new AuditLogCreateEvent(eventInfo, (new AuditLog())
          .with(paramAuditLog -> paramAuditLog.id = Long.valueOf(42L))
          .with(paramAuditLog -> paramAuditLog.insertUser = paramUser.email)
          .with(paramAuditLog -> paramAuditLog.message = "Updated the user with Id [" + String.valueOf(paramUser.id) + "] and loginId [" + paramUser.email + "]")
          .with(paramAuditLog -> paramAuditLog.insertInstant = ZonedDateTime.now(ZoneOffset.UTC))
          .with(paramAuditLog -> paramAuditLog.oldValue = paramUser)
          .with(paramAuditLog -> paramAuditLog.newValue = (new User(paramUser)).with(()).with(()).with(()))


          
          .with(paramAuditLog -> paramAuditLog.reason = "User was updated.")));
    this.EventLogCreateExample = buildEvent(new EventLogCreateEvent(new EventLog(EventLogType.Debug, "Event log create")));
    this.KickstartSuccessExample = buildEvent(new KickstartSuccessEvent(UUID.randomUUID()));
    this.EntityCreateExample = buildEvent(new EntityCreateEvent(eventInfo, entity));
    this.EntityCreateCompleteExample = buildEvent(new EntityCreateCompleteEvent(eventInfo, entity));
    this.EntityDeleteExample = buildEvent(new EntityDeleteEvent(eventInfo, entity));
    this.EntityDeleteCompleteExample = buildEvent(new EntityDeleteCompleteEvent(eventInfo, entity));
    this.EntityUpdateExample = buildEvent(new EntityUpdateEvent(eventInfo, entity, (new Entity(entity)).with(paramEntity -> paramEntity.name = "Changed name")));
    this.EntityUpdateCompleteExample = buildEvent(new EntityUpdateCompleteEvent(eventInfo, entity, (new Entity(entity)).with(paramEntity -> paramEntity.name = "Changed name")));
    this.GroupCreateCompleteExample = buildEvent(new GroupCreateCompleteEvent(eventInfo, group.sort()));
    this.GroupCreateExample = buildEvent(new GroupCreateEvent(eventInfo, group.sort()));
    this.GroupDeleteCompleteExample = buildEvent(new GroupDeleteCompleteEvent(eventInfo, group.sort()));
    this.GroupDeleteExample = buildEvent(new GroupDeleteEvent(eventInfo, group.sort()));
    this.GroupMemberAddCompleteExample = buildEvent(new GroupMemberAddCompleteEvent(eventInfo, group.sort(), List.of((new GroupMember())
            .with(paramGroupMember -> paramGroupMember.id = UUID.randomUUID())
            .with(paramGroupMember -> paramGroupMember.insertInstant = paramZonedDateTime)
            .with(paramGroupMember -> paramGroupMember.userId = paramUser.id))));
    this.GroupMemberAddExample = buildEvent(new GroupMemberAddEvent(eventInfo, group.sort(), List.of((new GroupMember())
            .with(paramGroupMember -> paramGroupMember.id = UUID.randomUUID())
            .with(paramGroupMember -> paramGroupMember.insertInstant = paramZonedDateTime)
            .with(paramGroupMember -> paramGroupMember.userId = paramUser.id))));
    this.GroupMemberRemoveCompleteExample = buildEvent(new GroupMemberRemoveCompleteEvent(eventInfo, group.sort(), List.of((new GroupMember())
            .with(paramGroupMember -> paramGroupMember.id = UUID.randomUUID())
            .with(paramGroupMember -> paramGroupMember.insertInstant = paramZonedDateTime)
            .with(paramGroupMember -> paramGroupMember.userId = paramUser.id))));
    this.GroupMemberRemoveExample = buildEvent(new GroupMemberRemoveEvent(eventInfo, group.sort(), List.of((new GroupMember())
            .with(paramGroupMember -> paramGroupMember.groupId = paramGroup.id)
            .with(paramGroupMember -> paramGroupMember.id = UUID.randomUUID())
            .with(paramGroupMember -> paramGroupMember.insertInstant = paramZonedDateTime)
            .with(paramGroupMember -> paramGroupMember.userId = paramUser.id))));
    this.GroupMemberUpdateCompleteExample = buildEvent(new GroupMemberUpdateCompleteEvent(eventInfo, group.sort(), List.of((new GroupMember())
            .with(paramGroupMember -> paramGroupMember.id = UUID.randomUUID())
            .with(paramGroupMember -> paramGroupMember.insertInstant = paramZonedDateTime)
            .with(paramGroupMember -> paramGroupMember.userId = paramUser.id))));
    this.GroupMemberUpdateExample = buildEvent(new GroupMemberUpdateEvent(eventInfo, group.sort(), List.of((new GroupMember())
            .with(paramGroupMember -> paramGroupMember.groupId = paramGroup.id)
            .with(paramGroupMember -> paramGroupMember.id = UUID.randomUUID())
            .with(paramGroupMember -> paramGroupMember.insertInstant = paramZonedDateTime)
            .with(paramGroupMember -> paramGroupMember.userId = paramUser.id))));
    this.GroupUpdateCompleteExample = buildEvent(new GroupUpdateCompleteEvent(eventInfo, group.sort(), (new Group(group)).with(paramGroup -> paramGroup.name = "Changed name")));
    this.GroupUpdateExample = buildEvent(new GroupUpdateEvent(eventInfo, group.sort(), (new Group(group)).with(paramGroup -> paramGroup.name = "Changed Name")));
    this.UserIdentityVerifiedExample = buildEvent(new UserIdentityVerifiedEvent(eventInfo, user.email, IdentityType.email.name, user));
    this.UserBulkCreateExample = buildEvent(new UserBulkCreateEvent(eventInfo, List.of(user)));
    this.UserCreateExample = buildEvent(new UserCreateEvent(eventInfo, user));
    this.UserCreateCompleteExample = buildEvent(new UserCreateCompleteEvent(eventInfo, user));
    this.UserDeactivateExample = buildEvent(new UserDeactivateEvent(eventInfo, user));
    this.UserDeleteExample = buildEvent(new UserDeleteEvent(eventInfo, user));
    this.UserDeleteCompleteExample = buildEvent(new UserDeleteCompleteEvent(eventInfo, user));
    String str3 = SecurityTools.secureRandom(64);
    this.UserIdentityProviderLinkExample = buildEvent(new UserIdentityProviderLinkEvent(eventInfo, (new IdentityProviderLink())
          .with(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderUserId = "42")
          .with(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderId = IdentityProviderType.Xbox.id)
          .with(paramIdentityProviderLink -> paramIdentityProviderLink.displayName = "JohnnyQ")
          .with(paramIdentityProviderLink -> paramIdentityProviderLink.tenantId = this.tenantId)
          .with(paramIdentityProviderLink -> paramIdentityProviderLink.token = paramString)
          .with(paramIdentityProviderLink -> paramIdentityProviderLink.userId = paramUser.id), user));
    this.UserIdentityProviderUnlinkExample = buildEvent(new UserIdentityProviderUnlinkEvent(eventInfo, (new IdentityProviderLink())
          .with(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderUserId = "42")
          .with(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderId = IdentityProviderType.Xbox.id)
          .with(paramIdentityProviderLink -> paramIdentityProviderLink.displayName = "JohnnyQ")
          .with(paramIdentityProviderLink -> paramIdentityProviderLink.tenantId = this.tenantId)
          .with(paramIdentityProviderLink -> paramIdentityProviderLink.token = paramString)
          .with(paramIdentityProviderLink -> paramIdentityProviderLink.userId = paramUser.id), user));
    this.UserLoginIdDuplicateOnCreateExample = buildEvent(new UserLoginIdDuplicateOnCreateEvent(eventInfo, "han@example.com", "hanSolo42", null, null, user, user));
    this.UserLoginIdDuplicateOnUpdateExample = buildEvent(new UserLoginIdDuplicateOnUpdateEvent(eventInfo, "han@example.com", "hanSolo42", null, null, user, user));
    this.UserReactivateExample = buildEvent(new UserReactivateEvent(eventInfo, user));
    this.UserUpdateExample = buildEvent(new UserUpdateEvent(eventInfo, user, user));
    this.UserUpdateCompleteExample = buildEvent(new UserUpdateCompleteEvent(eventInfo, user, user));
    UUID uUID = UUID.randomUUID();
    UserRegistration userRegistration1 = UserTestTools.baseRegistration();
    userRegistration1.applicationId = uUID;
    this.UserRegistrationCreateExample = buildEvent(new UserRegistrationCreateEvent(eventInfo, uUID, userRegistration1, new User(user
            .with(paramUser -> paramUser.getRegistrations().clear()))));
    this.UserRegistrationCreateCompleteExample = buildEvent(new UserRegistrationCreateCompleteEvent(eventInfo, uUID, userRegistration1, new User(user
            .with(paramUser -> paramUser.getRegistrations().clear()))));
    userRegistration1.verified = true;
    this.UserRegistrationVerifiedExample = buildEvent(new UserRegistrationVerifiedEvent(eventInfo, uUID, userRegistration1, new User(user
            .with(paramUser -> paramUser.getRegistrations().clear()))));
    UserRegistration userRegistration2 = UserTestTools.baseRegistration();
    userRegistration2.applicationId = uUID;
    userRegistration2.username = "new username";
    this.UserRegistrationUpdateExample = buildEvent(new UserRegistrationUpdateEvent(eventInfo, uUID, userRegistration1, userRegistration2, new User(user
            .with(paramUser -> paramUser.getRegistrations().clear()))));
    this.UserRegistrationUpdateCompleteExample = buildEvent(new UserRegistrationUpdateCompleteEvent(eventInfo, uUID, userRegistration1, userRegistration2, new User(user
            .with(paramUser -> paramUser.getRegistrations().clear()))));
    this.UserRegistrationDeleteExample = buildEvent(new UserRegistrationDeleteEvent(eventInfo, uUID, userRegistration1, new User(user
            .with(paramUser -> paramUser.getRegistrations().clear()))));
    this.UserRegistrationDeleteCompleteExample = buildEvent(new UserRegistrationDeleteCompleteEvent(eventInfo, uUID, userRegistration1, new User(user
            .with(paramUser -> paramUser.getRegistrations().clear()))));
    this.UserLoginSuccessExample = buildEvent(new UserLoginSuccessEvent(eventInfo, uUID, BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID, AuthenticationType.PASSWORD.name(), user));
    this.UserLoginFailedExample = buildEvent(new UserLoginFailedEvent(eventInfo, uUID, AuthenticationType.PASSWORD.name(), user));
    this.UserLoginNewDeviceExample = buildEvent(new UserLoginNewDeviceEvent(this.frontEndSupport.buildEventInfo(null), uUID, BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID, AuthenticationType.PASSWORD

          
          .name(), user));
    this.UserLoginSuspiciousExample = buildEvent(new UserLoginSuspiciousEvent(eventInfo, uUID, BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID, AuthenticationType.PASSWORD.name(), user, Set.of(AuthenticationThreats.ImpossibleTravel)));
    this.UserTwoFactorChallengeExample = buildEvent(new UserTwoFactorChallengeEvent(eventInfo, RiskLevel.LOW.name(), uUID, user));
    this.UserTwoFactorFailedAttemptExample = buildEvent(new UserTwoFactorFailedAttemptEvent(eventInfo, RiskLevel.LOW.name(), MessageType.SMS.name(), "sms", uUID, user));
    this.UserEmailUpdateExample = buildEvent(new UserEmailUpdateEvent(eventInfo, "smuggler02@hotmail.com", user));
    this.UserEmailVerifiedExample = buildEvent(new UserEmailVerifiedEvent(eventInfo, user));
    this.UserIdentityUpdateExample = buildEvent(new UserIdentityUpdateEvent(eventInfo, "+15555551111", "+15555552222", IdentityType.phoneNumber.name, user));
    this.JWTRefreshTokenRevokeExample = buildEvent(new JWTRefreshTokenRevokeEvent(eventInfo, user, list3.get(0), 900));
    this.JWTPublicKeyUpdateExample = buildEvent(new JWTPublicKeyUpdateEvent(eventInfo, list3.get(0)));
    this.UserPasswordBreachExample = buildEvent(new UserPasswordBreachEvent(eventInfo, user));
    this.UserPasswordResetSendExample = buildEvent(new UserPasswordResetSendEvent(eventInfo, user));
    this.UserPasswordResetStartExample = buildEvent(new UserPasswordResetStartEvent(eventInfo, user));
    this.UserPasswordResetSuccessExample = buildEvent(new UserPasswordResetSuccessEvent(eventInfo, user));
    this.UserPasswordUpdateExample = buildEvent(new UserPasswordUpdateEvent(eventInfo, user));
    String str4 = (this.jwtService.createJWT((Tenant)this.tenants.get(this.fusionAuthTenantId), user, AuthenticationType.PASSWORD, application, Map.of(), null, null, JWTType.AccessToken, (Set)Collections.emptySet(), null)).encodedJWT;
    String str5 = (this.jwtService.createJWT((Tenant)this.tenants.get(this.fusionAuthTenantId), user, AuthenticationType.PASSWORD, application, Map.of(), null, null, JWTType.AccessToken, (Set)Collections.emptySet(), null)).encodedJWT;
    String str6 = "ze9fi6Y9sMSf3yWp3aaO2w7AMav2MFdiMIi2GObrAi-i3248oo0jTQ";
    this.JWTRefreshExample = buildEvent(new JWTRefreshEvent(eventInfo, list3.get(0), str5, str4, str6, user.id));
    this.UserTwoFactorMethodAddExample = buildEvent(new UserTwoFactorMethodAddEvent(eventInfo, (new TwoFactorMethod("sms"))
          .with(paramTwoFactorMethod -> paramTwoFactorMethod.id = TwoFactorTools.generateUniqueTwoFactorMethodId(paramUser))
          .with(paramTwoFactorMethod -> paramTwoFactorMethod.mobilePhone = "555-555-5555")
          .secure(), user));
    this.UserTwoFactorMethodRemoveExample = buildEvent(new UserTwoFactorMethodRemoveEvent(eventInfo, (new TwoFactorMethod("email"))
          .with(paramTwoFactorMethod -> paramTwoFactorMethod.id = TwoFactorTools.generateUniqueTwoFactorMethodId(paramUser))
          .with(paramTwoFactorMethod -> paramTwoFactorMethod.email = paramUser.email)
          .secure(), user));
    this.UserTwoFactorSuccessExample = buildEvent(new UserTwoFactorSuccessEvent(eventInfo, RiskLevel.LOW.name(), MessageType.SMS.name(), "sms", uUID, user));
    if (this.selectedEventType != null)
      try {
        this.field.set(this, this.selectedEventType);
      } catch (IllegalAccessException illegalAccessException) {
        throw new RuntimeException(illegalAccessException);
      }  
  }
  
  @ValidationMethod
  public void validate() {
    try {
      String str = (this.type == EventType.UserAction) ? ((this.UserAction_CodeExample != null) ? "UserAction_CodeExample" : "UserAction_TemporalExample") : (this.type.name() + "Example");
      this.field = getClass().getDeclaredField(str);
      this.selectedEventType = (String)this.field.get(this);
    } catch (Exception exception) {
      throw new ErrorException(exception, new Object[0]);
    } 
    try {
      this.eventRequest = (EventRequest)this.objectMapper.readValue(this.selectedEventType, EventRequest.class);
    } catch (JacksonException jacksonException) {
      this.frontEndSupport.addGeneralError("[invalidJSON]", new Object[] { jacksonException.getMessage() });
    } 
  }
  
  private <T extends BaseEvent> String buildEvent(T paramT) {
    EventRequest eventRequest = new EventRequest((BaseEvent)paramT);
    eventRequest.event.id = UUID.randomUUID();
    eventRequest.event.createInstant = ZonedDateTime.now(ZoneOffset.UTC);
    eventRequest.event.tenantId = this.fusionAuthTenantId;
    return eventRequest.toString();
  }
  
  private Key signingKey(Webhook paramWebhook) {
    if (!paramWebhook.signatureConfiguration.enabled || paramWebhook.signatureConfiguration.signingKeyId == null)
      return null; 
    return this.keyReaderService.retrieveById(paramWebhook.signatureConfiguration.signingKeyId);
  }
}
