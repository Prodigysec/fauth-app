package io.fusionauth.api.service.event;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.inversoft.rest.ProxyInfoSupplier;
import io.fusionauth.api.domain.FusionAuthNodeMapper;
import io.fusionauth.api.domain.guice.FusionAuthInternalAPIKey;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.cache.WebhookCache;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.ip.LocationService;
import io.fusionauth.api.service.messaging.KafkaService;
import io.fusionauth.api.service.messenger.MessengerServiceProxy;
import io.fusionauth.api.service.useraction.UserActionService;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EmailConfiguration;
import io.fusionauth.domain.EventConfiguration;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TransactionType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.WebhookEventLog;
import io.fusionauth.domain.WebhookEventResult;
import io.fusionauth.domain.event.BaseEvent;
import io.fusionauth.domain.event.BaseUserEvent;
import io.fusionauth.domain.event.EventRequest;
import io.fusionauth.domain.event.EventType;
import io.fusionauth.domain.event.JWTRefreshTokenRevokeEvent;
import io.fusionauth.domain.event.ObjectIdentifiable;
import io.fusionauth.domain.event.UserActionEvent;
import io.fusionauth.domain.event.UserEmailUpdateEvent;
import io.fusionauth.domain.event.UserEmailVerifiedEvent;
import io.fusionauth.domain.event.UserLoginIdDuplicateOnCreateEvent;
import io.fusionauth.domain.event.UserLoginIdDuplicateOnUpdateEvent;
import io.fusionauth.domain.event.UserLoginNewDeviceEvent;
import io.fusionauth.domain.event.UserLoginSuspiciousEvent;
import io.fusionauth.domain.event.UserPasswordResetSuccessEvent;
import io.fusionauth.domain.event.UserPasswordUpdateEvent;
import io.fusionauth.domain.event.UserTwoFactorMethodAddEvent;
import io.fusionauth.domain.event.UserTwoFactorMethodRemoveEvent;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.primeframework.mvc.ErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEventService implements EventService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEventService.class);
  
  private final EmailProxy emailProxy;
  
  private final EventExecutorService eventExecutorService;
  
  private final EventRetryQueue eventRetryQueue;
  
  private final FusionAuthNodeMapper fusionAuthNodeMapper;
  
  private final Injector injector;
  
  private final APIKey internalAPIKey;
  
  private final KafkaService kafkaService;
  
  private final KeyCache keyCache;
  
  private final LocationService locationService;
  
  private final MessengerServiceProxy messengerServiceProxy;
  
  private final MetricRegistry metricRegistry;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  private final UserActionService userActionService;
  
  private final WebhookCache webhookCache;
  
  private final WebhookEventLogService webhookEventLogService;
  
  @Inject
  public DefaultEventService(EmailProxy paramEmailProxy, EventExecutorService paramEventExecutorService, FusionAuthNodeMapper paramFusionAuthNodeMapper, EventRetryQueue paramEventRetryQueue, Injector paramInjector, @FusionAuthInternalAPIKey APIKey paramAPIKey, KafkaService paramKafkaService, KeyCache paramKeyCache, LocationService paramLocationService, MessengerServiceProxy paramMessengerServiceProxy, MetricRegistry paramMetricRegistry, ProxyInfoSupplier paramProxyInfoSupplier, UserActionService paramUserActionService, WebhookCache paramWebhookCache, WebhookEventLogService paramWebhookEventLogService) {
    this.emailProxy = paramEmailProxy;
    this.eventExecutorService = paramEventExecutorService;
    this.fusionAuthNodeMapper = paramFusionAuthNodeMapper;
    this.eventRetryQueue = paramEventRetryQueue;
    this.injector = paramInjector;
    this.internalAPIKey = paramAPIKey;
    this.kafkaService = paramKafkaService;
    this.keyCache = paramKeyCache;
    this.locationService = paramLocationService;
    this.messengerServiceProxy = paramMessengerServiceProxy;
    this.metricRegistry = paramMetricRegistry;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
    this.userActionService = paramUserActionService;
    this.webhookCache = paramWebhookCache;
    this.webhookEventLogService = paramWebhookEventLogService;
  }
  
  public void send(Tenant paramTenant, Application paramApplication, BaseEvent paramBaseEvent) {
    if (paramBaseEvent.info != null && paramBaseEvent.info.ipAddress != null)
      paramBaseEvent.info.location = this.locationService.ipToLocation(paramBaseEvent.info.ipAddress); 
    EventManifest eventManifest = (paramBaseEvent instanceof io.fusionauth.domain.event.InstanceEvent) ? generateManifestForInstanceEvent(paramBaseEvent, false) : generateManifest(paramTenant, paramBaseEvent, false);
    sendEvent(paramBaseEvent, eventManifest, paramTenant);
    handleEmail(paramTenant, paramApplication, paramBaseEvent);
    handleMessage(paramTenant, paramApplication, paramBaseEvent);
  }
  
  public EventSenderResult sendTest(BaseEvent paramBaseEvent, EventSender paramEventSender) {
    EventManifest eventManifest = (paramBaseEvent instanceof io.fusionauth.domain.event.InstanceEvent) ? generateManifestForInstanceEvent(paramBaseEvent, true) : generateManifest(null, paramBaseEvent, true);
    eventManifest.senders = List.of(paramEventSender);
    List<Future<EventSenderResult>> list = sendEvent(paramBaseEvent, eventManifest, null);
    try {
      return ((Future<EventSenderResult>)list.get(0)).get();
    } catch (Exception exception) {
      throw new ErrorException(exception, new Object[0]);
    } 
  }
  
  private WebhookEventSender buildInternalWebhookSender(FusionAuthNodeMapper.FusionAuthNode paramFusionAuthNode) {
    return new WebhookEventSender(this.metricRegistry, this.proxyInfoSupplier, null, (new Webhook())



        
        .with(paramWebhook -> paramWebhook.headers.put("Authorization", this.internalAPIKey.key))
        .with(paramWebhook -> paramWebhook.connectTimeout = Integer.valueOf(1000))
        .with(paramWebhook -> paramWebhook.readTimeout = Integer.valueOf(2000))
        .with(paramWebhook -> paramWebhook.url = URI.create(paramFusionAuthNode.url + "/internal/webhook")), null, (ObjectMapper)this.injector
        .getInstance(ObjectMapper.class));
  }
  
  private WebhookEventSender buildWebhookEventSender(Webhook paramWebhook) {
    return new WebhookEventSender(this.metricRegistry, this.proxyInfoSupplier, 

        
        signingKey(paramWebhook), 

        
        (paramWebhook.sslCertificateKeyId != null) ? (
        new Webhook(paramWebhook)).with(paramWebhook2 -> paramWebhook2.sslCertificate = ((Key)this.keyCache.get(paramWebhook1.sslCertificateKeyId)).certificate) : 
        paramWebhook, this.webhookEventLogService, (ObjectMapper)this.injector
        .getInstance(ObjectMapper.class));
  }
  
  private Stream<Webhook> eligibleWebhooks(UUID paramUUID1, BaseEvent paramBaseEvent, UUID paramUUID2, boolean paramBoolean) {
    List<Webhook> list = (paramBaseEvent instanceof io.fusionauth.domain.event.InstanceEvent) ? this.webhookCache.getAll() : this.webhookCache.retrieveForTenantAndGlobals(paramUUID1);
    EventType eventType = paramBaseEvent.getType();
    return list.stream()
      .filter(paramWebhook -> !paramWebhook.id.equals(paramUUID))
      .filter(paramWebhook -> (paramBoolean || (paramWebhook.eventsEnabled.containsKey(paramEventType) && ((Boolean)paramWebhook.eventsEnabled.get(paramEventType)).booleanValue())));
  }
  
  private EventManifest generateManifest(Tenant paramTenant, BaseEvent paramBaseEvent, boolean paramBoolean) {
    EventType eventType = paramBaseEvent.getType();
    paramBaseEvent.tenantId = (paramTenant != null) ? paramTenant.id : null;
    EventConfiguration.EventConfigurationData eventConfigurationData = (paramTenant == null) ? null : paramTenant.eventConfiguration.events.get(eventType);
    if (eventType == EventType.AuditLogCreate)
      eventConfigurationData = new EventConfiguration.EventConfigurationData(true, TransactionType.None); 
    if (eventConfigurationData != null && paramBaseEvent instanceof io.fusionauth.domain.event.NonTransactionalEvent)
      eventConfigurationData.transactionType = TransactionType.None; 
    if (!paramBoolean && (eventConfigurationData == null || !eventConfigurationData.enabled))
      return new EventManifest(); 
    List<KafkaEventSender> list = (List)eligibleWebhooks(paramBaseEvent.tenantId, paramBaseEvent, null, paramBoolean).map(this::buildWebhookEventSender).collect(Collectors.toList());
    if (this.kafkaService.isEnabled())
      list.add(new KafkaEventSender(this.kafkaService)); 
    TransactionType transactionType = TransactionType.AbsoluteMajority;
    if (!paramBoolean)
      if (eventType == EventType.UserAction) {
        UserAction userAction = this.userActionService.retrieveByName(((UserActionEvent)paramBaseEvent).action);
        if (userAction != null && userAction.transactionType != null)
          transactionType = userAction.transactionType; 
      } else if (eventConfigurationData.transactionType != null) {
        transactionType = eventConfigurationData.transactionType;
      }  
    return new EventManifest((List)list, transactionType, paramBoolean);
  }
  
  private EventManifest generateManifestForInstanceEvent(BaseEvent paramBaseEvent, boolean paramBoolean) {
    // Byte code:
    //   0: aconst_null
    //   1: astore_3
    //   2: aload_1
    //   3: instanceof io/fusionauth/domain/event/EventLogCreateEvent
    //   6: ifeq -> 43
    //   9: aload_1
    //   10: checkcast io/fusionauth/domain/event/EventLogCreateEvent
    //   13: astore #4
    //   15: aload #4
    //   17: getfield eventLog : Lio/fusionauth/domain/EventLog;
    //   20: astore #6
    //   22: aload #6
    //   24: instanceof io/fusionauth/api/service/event/WebhookEventSender$WebhookErrorEventLog
    //   27: ifeq -> 43
    //   30: aload #6
    //   32: checkcast io/fusionauth/api/service/event/WebhookEventSender$WebhookErrorEventLog
    //   35: astore #5
    //   37: aload #5
    //   39: getfield webhookId : Ljava/util/UUID;
    //   42: astore_3
    //   43: aload_0
    //   44: aconst_null
    //   45: aload_1
    //   46: aload_3
    //   47: iload_2
    //   48: invokevirtual eligibleWebhooks : (Ljava/util/UUID;Lio/fusionauth/domain/event/BaseEvent;Ljava/util/UUID;Z)Ljava/util/stream/Stream;
    //   51: aload_0
    //   52: <illegal opcode> apply : (Lio/fusionauth/api/service/event/DefaultEventService;)Ljava/util/function/Function;
    //   57: invokeinterface map : (Ljava/util/function/Function;)Ljava/util/stream/Stream;
    //   62: invokestatic toList : ()Ljava/util/stream/Collector;
    //   65: invokeinterface collect : (Ljava/util/stream/Collector;)Ljava/lang/Object;
    //   70: checkcast java/util/List
    //   73: astore #4
    //   75: aload_0
    //   76: getfield kafkaService : Lio/fusionauth/api/service/messaging/KafkaService;
    //   79: invokeinterface isEnabled : ()Z
    //   84: ifeq -> 130
    //   87: aload_1
    //   88: instanceof io/fusionauth/domain/event/EventLogCreateEvent
    //   91: ifeq -> 111
    //   94: aload_1
    //   95: checkcast io/fusionauth/domain/event/EventLogCreateEvent
    //   98: astore #5
    //   100: aload #5
    //   102: getfield eventLog : Lio/fusionauth/domain/EventLog;
    //   105: instanceof io/fusionauth/api/service/messaging/KafkaService$KafkaErrorEventLog
    //   108: ifne -> 130
    //   111: aload #4
    //   113: new io/fusionauth/api/service/event/KafkaEventSender
    //   116: dup
    //   117: aload_0
    //   118: getfield kafkaService : Lio/fusionauth/api/service/messaging/KafkaService;
    //   121: invokespecial <init> : (Lio/fusionauth/api/service/messaging/KafkaService;)V
    //   124: invokeinterface add : (Ljava/lang/Object;)Z
    //   129: pop
    //   130: iload_2
    //   131: ifeq -> 140
    //   134: getstatic io/fusionauth/domain/TransactionType.AbsoluteMajority : Lio/fusionauth/domain/TransactionType;
    //   137: goto -> 143
    //   140: getstatic io/fusionauth/domain/TransactionType.None : Lio/fusionauth/domain/TransactionType;
    //   143: astore #5
    //   145: new io/fusionauth/api/service/event/DefaultEventService$EventManifest
    //   148: dup
    //   149: aload #4
    //   151: aload #5
    //   153: iload_2
    //   154: invokespecial <init> : (Ljava/util/List;Lio/fusionauth/domain/TransactionType;Z)V
    //   157: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #258	-> 0
    //   #259	-> 2
    //   #260	-> 15
    //   #261	-> 37
    //   #266	-> 43
    //   #267	-> 57
    //   #268	-> 62
    //   #273	-> 75
    //   #274	-> 94
    //   #276	-> 111
    //   #280	-> 130
    //   #281	-> 145
  }
  
  private void handleEmail(Tenant paramTenant, Application paramApplication, BaseEvent paramBaseEvent) {
    Object object;
    if (paramTenant == null)
      return; 
    Application.ApplicationEmailConfiguration applicationEmailConfiguration = (paramApplication != null) ? paramApplication.emailConfiguration : null;
    EmailConfiguration emailConfiguration = paramTenant.emailConfiguration;
    if (paramBaseEvent instanceof BaseUserEvent) {
      BaseUserEvent baseUserEvent = (BaseUserEvent)paramBaseEvent;
      object = baseUserEvent.user;
    } else {
      object = null;
    } 
    switch (paramBaseEvent.getType()) {
      case UserEmailUpdate:
        sendEmail(applicationEmailConfiguration, paramApplicationEmailConfiguration -> paramApplicationEmailConfiguration.emailUpdateEmailTemplateId, emailConfiguration.emailUpdateEmailTemplateId, () -> this.emailProxy.sendUserEmailUpdate(paramTenant, paramApplication, paramUser, (UserEmailUpdateEvent)paramBaseEvent));
        break;
      case UserEmailVerified:
        sendEmail(applicationEmailConfiguration, paramApplicationEmailConfiguration -> paramApplicationEmailConfiguration.emailVerifiedEmailTemplateId, emailConfiguration.emailVerifiedEmailTemplateId, () -> this.emailProxy.sendUserEmailVerified(paramTenant, paramApplication, paramUser, (UserEmailVerifiedEvent)paramBaseEvent));
        break;
      case UserLoginIdDuplicateOnCreate:
        sendEmail(applicationEmailConfiguration, paramApplicationEmailConfiguration -> paramApplicationEmailConfiguration.loginIdInUseOnCreateEmailTemplateId, emailConfiguration.loginIdInUseOnCreateEmailTemplateId, () -> this.emailProxy.sendLoginIdDuplicateOnCreate(paramTenant, paramApplication, paramUser, (UserLoginIdDuplicateOnCreateEvent)paramBaseEvent));
        break;
      case UserLoginIdDuplicateOnUpdate:
        sendEmail(applicationEmailConfiguration, paramApplicationEmailConfiguration -> paramApplicationEmailConfiguration.loginIdInUseOnUpdateEmailTemplateId, emailConfiguration.loginIdInUseOnUpdateEmailTemplateId, () -> this.emailProxy.sendLoginIdDuplicateOnUpdate(paramTenant, paramApplication, paramUser, (UserLoginIdDuplicateOnUpdateEvent)paramBaseEvent));
        break;
      case UserLoginNewDevice:
        sendEmail(applicationEmailConfiguration, paramApplicationEmailConfiguration -> paramApplicationEmailConfiguration.loginNewDeviceEmailTemplateId, emailConfiguration.loginNewDeviceEmailTemplateId, () -> this.emailProxy.sendUserLoginNewDevice(paramTenant, paramApplication, paramUser, (UserLoginNewDeviceEvent)paramBaseEvent));
        break;
      case UserLoginSuspicious:
        sendEmail(applicationEmailConfiguration, paramApplicationEmailConfiguration -> paramApplicationEmailConfiguration.loginSuspiciousEmailTemplateId, emailConfiguration.loginSuspiciousEmailTemplateId, () -> this.emailProxy.sendUserLoginSuspicious(paramTenant, paramApplication, paramUser, (UserLoginSuspiciousEvent)paramBaseEvent));
        break;
      case UserPasswordResetSuccess:
        sendEmail(applicationEmailConfiguration, paramApplicationEmailConfiguration -> paramApplicationEmailConfiguration.passwordResetSuccessEmailTemplateId, emailConfiguration.passwordResetSuccessEmailTemplateId, () -> this.emailProxy.sendUserPasswordResetSuccess(paramTenant, paramApplication, paramUser, (UserPasswordResetSuccessEvent)paramBaseEvent));
        break;
      case UserPasswordUpdate:
        sendEmail(applicationEmailConfiguration, paramApplicationEmailConfiguration -> paramApplicationEmailConfiguration.passwordUpdateEmailTemplateId, emailConfiguration.passwordUpdateEmailTemplateId, () -> this.emailProxy.sendUserPasswordUpdate(paramTenant, paramApplication, paramUser, (UserPasswordUpdateEvent)paramBaseEvent));
        break;
      case UserTwoFactorMethodAdd:
        sendEmail(applicationEmailConfiguration, paramApplicationEmailConfiguration -> paramApplicationEmailConfiguration.twoFactorMethodAddEmailTemplateId, emailConfiguration.twoFactorMethodAddEmailTemplateId, () -> this.emailProxy.sendUserTwoFactorAdd(paramTenant, paramApplication, paramUser, (UserTwoFactorMethodAddEvent)paramBaseEvent));
        break;
      case UserTwoFactorMethodRemove:
        sendEmail(applicationEmailConfiguration, paramApplicationEmailConfiguration -> paramApplicationEmailConfiguration.twoFactorMethodRemoveEmailTemplateId, emailConfiguration.twoFactorMethodRemoveEmailTemplateId, () -> this.emailProxy.sendUserTwoFactorRemove(paramTenant, paramApplication, paramUser, (UserTwoFactorMethodRemoveEvent)paramBaseEvent));
        break;
    } 
  }
  
  private void handleMessage(Tenant paramTenant, Application paramApplication, BaseEvent paramBaseEvent) {
    // Byte code:
    //   0: aload_1
    //   1: ifnull -> 14
    //   4: aload_1
    //   5: getfield phoneConfiguration : Lio/fusionauth/domain/TenantPhoneConfiguration;
    //   8: getfield messengerId : Ljava/util/UUID;
    //   11: ifnonnull -> 15
    //   14: return
    //   15: aload_3
    //   16: instanceof io/fusionauth/domain/event/BaseUserEvent
    //   19: ifeq -> 419
    //   22: aload_3
    //   23: checkcast io/fusionauth/domain/event/BaseUserEvent
    //   26: astore #4
    //   28: aload #4
    //   30: getfield user : Lio/fusionauth/domain/User;
    //   33: ifnull -> 419
    //   36: new io/fusionauth/domain/User
    //   39: dup
    //   40: aload #4
    //   42: getfield user : Lio/fusionauth/domain/User;
    //   45: invokespecial <init> : (Lio/fusionauth/domain/User;)V
    //   48: invokevirtual secure : ()Lio/fusionauth/domain/User;
    //   51: invokevirtual sort : ()Lio/fusionauth/domain/User;
    //   54: astore #5
    //   56: aload #4
    //   58: dup
    //   59: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
    //   62: pop
    //   63: astore #6
    //   65: iconst_0
    //   66: istore #7
    //   68: aload #6
    //   70: iload #7
    //   72: <illegal opcode> typeSwitch : (Lio/fusionauth/domain/event/BaseUserEvent;I)I
    //   77: tableswitch default -> 396, 0 -> 132, 1 -> 171, 2 -> 196, 3 -> 221, 4 -> 246, 5 -> 271, 6 -> 296, 7 -> 321, 8 -> 346, 9 -> 371
    //   132: aload #6
    //   134: checkcast io/fusionauth/domain/event/UserIdentityUpdateEvent
    //   137: astore #8
    //   139: getstatic io/fusionauth/domain/IdentityType.phoneNumber : Lio/fusionauth/domain/IdentityType;
    //   142: aload #8
    //   144: getfield loginIdType : Ljava/lang/String;
    //   147: invokevirtual is : (Ljava/lang/String;)Z
    //   150: ifeq -> 396
    //   153: aload_0
    //   154: getfield messengerServiceProxy : Lio/fusionauth/api/service/messenger/MessengerServiceProxy;
    //   157: aload_1
    //   158: aload_2
    //   159: aload #5
    //   161: aload #8
    //   163: invokeinterface sendUserIdentityUpdateEvent : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/domain/event/UserIdentityUpdateEvent;)V
    //   168: goto -> 396
    //   171: aload #6
    //   173: checkcast io/fusionauth/domain/event/UserIdentityVerifiedEvent
    //   176: astore #9
    //   178: aload_0
    //   179: getfield messengerServiceProxy : Lio/fusionauth/api/service/messenger/MessengerServiceProxy;
    //   182: aload_1
    //   183: aload_2
    //   184: aload #5
    //   186: aload #9
    //   188: invokeinterface sendUserIdentityVerifiedEvent : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/domain/event/UserIdentityVerifiedEvent;)V
    //   193: goto -> 396
    //   196: aload #6
    //   198: checkcast io/fusionauth/domain/event/UserLoginIdDuplicateOnUpdateEvent
    //   201: astore #10
    //   203: aload_0
    //   204: getfield messengerServiceProxy : Lio/fusionauth/api/service/messenger/MessengerServiceProxy;
    //   207: aload_1
    //   208: aload_2
    //   209: aload #5
    //   211: aload #10
    //   213: invokeinterface sendUserLoginIdDuplicateOnUpdate : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/domain/event/UserLoginIdDuplicateOnUpdateEvent;)V
    //   218: goto -> 396
    //   221: aload #6
    //   223: checkcast io/fusionauth/domain/event/UserLoginIdDuplicateOnCreateEvent
    //   226: astore #11
    //   228: aload_0
    //   229: getfield messengerServiceProxy : Lio/fusionauth/api/service/messenger/MessengerServiceProxy;
    //   232: aload_1
    //   233: aload_2
    //   234: aload #5
    //   236: aload #11
    //   238: invokeinterface sendUserLoginIdDuplicateOnCreate : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/domain/event/UserLoginIdDuplicateOnCreateEvent;)V
    //   243: goto -> 396
    //   246: aload #6
    //   248: checkcast io/fusionauth/domain/event/UserLoginNewDeviceEvent
    //   251: astore #12
    //   253: aload_0
    //   254: getfield messengerServiceProxy : Lio/fusionauth/api/service/messenger/MessengerServiceProxy;
    //   257: aload_1
    //   258: aload_2
    //   259: aload #5
    //   261: aload #12
    //   263: invokeinterface sendUserLoginNewDeviceEvent : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/domain/event/UserLoginNewDeviceEvent;)V
    //   268: goto -> 396
    //   271: aload #6
    //   273: checkcast io/fusionauth/domain/event/UserLoginSuspiciousEvent
    //   276: astore #13
    //   278: aload_0
    //   279: getfield messengerServiceProxy : Lio/fusionauth/api/service/messenger/MessengerServiceProxy;
    //   282: aload_1
    //   283: aload_2
    //   284: aload #5
    //   286: aload #13
    //   288: invokeinterface sendUserLoginSuspiciousEvent : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/domain/event/UserLoginSuspiciousEvent;)V
    //   293: goto -> 396
    //   296: aload #6
    //   298: checkcast io/fusionauth/domain/event/UserPasswordResetSuccessEvent
    //   301: astore #14
    //   303: aload_0
    //   304: getfield messengerServiceProxy : Lio/fusionauth/api/service/messenger/MessengerServiceProxy;
    //   307: aload_1
    //   308: aload_2
    //   309: aload #5
    //   311: aload #14
    //   313: invokeinterface sendUserPasswordResetSuccessEvent : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/domain/event/UserPasswordResetSuccessEvent;)V
    //   318: goto -> 396
    //   321: aload #6
    //   323: checkcast io/fusionauth/domain/event/UserPasswordUpdateEvent
    //   326: astore #15
    //   328: aload_0
    //   329: getfield messengerServiceProxy : Lio/fusionauth/api/service/messenger/MessengerServiceProxy;
    //   332: aload_1
    //   333: aload_2
    //   334: aload #5
    //   336: aload #15
    //   338: invokeinterface sendUserPasswordUpdateEvent : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/domain/event/UserPasswordUpdateEvent;)V
    //   343: goto -> 396
    //   346: aload #6
    //   348: checkcast io/fusionauth/domain/event/UserTwoFactorMethodAddEvent
    //   351: astore #16
    //   353: aload_0
    //   354: getfield messengerServiceProxy : Lio/fusionauth/api/service/messenger/MessengerServiceProxy;
    //   357: aload_1
    //   358: aload_2
    //   359: aload #5
    //   361: aload #16
    //   363: invokeinterface sendUserTwoFactorMethodAddEvent : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/domain/event/UserTwoFactorMethodAddEvent;)V
    //   368: goto -> 396
    //   371: aload #6
    //   373: checkcast io/fusionauth/domain/event/UserTwoFactorMethodRemoveEvent
    //   376: astore #17
    //   378: aload_0
    //   379: getfield messengerServiceProxy : Lio/fusionauth/api/service/messenger/MessengerServiceProxy;
    //   382: aload_1
    //   383: aload_2
    //   384: aload #5
    //   386: aload #17
    //   388: invokeinterface sendUserTwoFactorMethodRemoveEvent : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/domain/event/UserTwoFactorMethodRemoveEvent;)V
    //   393: goto -> 396
    //   396: goto -> 419
    //   399: astore #6
    //   401: getstatic io/fusionauth/api/service/event/DefaultEventService.logger : Lorg/slf4j/Logger;
    //   404: ldc_w 'Error sending message for {} event:'
    //   407: aload #4
    //   409: invokevirtual getType : ()Lio/fusionauth/domain/event/EventType;
    //   412: aload #6
    //   414: invokeinterface error : (Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V
    //   419: return
    // Line number table:
    //   Java source line number -> byte code offset
    //   #318	-> 0
    //   #319	-> 14
    //   #322	-> 15
    //   #323	-> 36
    //   #326	-> 56
    //   #327	-> 132
    //   #328	-> 139
    //   #329	-> 153
    //   #332	-> 171
    //   #333	-> 178
    //   #334	-> 196
    //   #335	-> 203
    //   #336	-> 221
    //   #337	-> 228
    //   #338	-> 246
    //   #339	-> 253
    //   #340	-> 271
    //   #341	-> 278
    //   #342	-> 296
    //   #343	-> 303
    //   #344	-> 321
    //   #345	-> 328
    //   #346	-> 346
    //   #347	-> 353
    //   #348	-> 371
    //   #349	-> 378
    //   #357	-> 396
    //   #355	-> 399
    //   #356	-> 401
    //   #359	-> 419
    // Exception table:
    //   from	to	target	type
    //   56	396	399	java/lang/Exception
  }
  
  private <T extends BaseEvent> void handleSpecialInternalWebhook(T paramT) {
    if (paramT instanceof JWTRefreshTokenRevokeEvent) {
      JWTRefreshTokenRevokeEvent jWTRefreshTokenRevokeEvent = (JWTRefreshTokenRevokeEvent)paramT;
      if (Application.FUSIONAUTH_APP_ID.equals(jWTRefreshTokenRevokeEvent.applicationId) || jWTRefreshTokenRevokeEvent.applicationTimeToLiveInSeconds
        .containsKey(Application.FUSIONAUTH_APP_ID)) {
        List<EventSender> list = (List)this.fusionAuthNodeMapper.retrieveAll().stream().map(this::buildInternalWebhookSender).collect(Collectors.toList());
        this.eventExecutorService.send((BaseEvent)paramT, list);
      } 
    } 
  }
  
  private <T extends BaseEvent> UUID resolveObjectIdForEvent(T paramT) {
    if (paramT instanceof ObjectIdentifiable)
      return ((ObjectIdentifiable)paramT).getLinkedObjectId(); 
    return null;
  }
  
  private void sendEmail(Application.ApplicationEmailConfiguration paramApplicationEmailConfiguration, Function<Application.ApplicationEmailConfiguration, UUID> paramFunction, UUID paramUUID, Runnable paramRunnable) {
    UUID uUID = (paramApplicationEmailConfiguration != null) ? paramFunction.apply(paramApplicationEmailConfiguration) : null;
    if (uUID == null && paramUUID == null)
      return; 
    paramRunnable.run();
  }
  
  private <T extends BaseEvent> List<Future<EventSenderResult>> sendEvent(T paramT, EventManifest paramEventManifest, Tenant paramTenant) {
    byte b = 0;
    ArrayList<EventSender> arrayList = new ArrayList();
    int i = paramEventManifest.senders.size();
    ((BaseEvent)paramT).createInstant = ZonedDateTime.now(ZoneOffset.UTC);
    if (((BaseEvent)paramT).id == null)
      ((BaseEvent)paramT).id = UUID.randomUUID(); 
    ((BaseEvent)paramT).tenantId = (paramTenant == null) ? null : paramTenant.id;
    handleSpecialInternalWebhook(paramT);
    if (paramEventManifest.senders.isEmpty())
      return List.of(); 
    if (!paramEventManifest.testing)
      this.webhookEventLogService.createWebhookEventLog(() -> (new WebhookEventLog()).with(()).with(()).with(()).with(())); 
    List<Future<EventSenderResult>> list = this.eventExecutorService.send((BaseEvent)paramT, paramEventManifest.senders);
    if (paramEventManifest.transactionType == TransactionType.None) {
      if (!paramEventManifest.testing)
        this.webhookEventLogService.updateWebhookEventLogResult(((BaseEvent)paramT).id, WebhookEventResult.Succeeded); 
      return list;
    } 
    for (Future<EventSenderResult> future : list) {
      try {
        EventSenderResult eventSenderResult = future.get();
        if (eventSenderResult.success) {
          b++;
          continue;
        } 
        arrayList.add(eventSenderResult.sender);
      } catch (Exception exception) {}
    } 
    boolean bool = paramEventManifest.transactionType.success(i, b);
    if (bool) {
      Objects.requireNonNull(this.eventRetryQueue);
      arrayList.stream().map(paramEventSender -> new EventRetryQueue.RetryPayload(paramBaseEvent, paramEventSender)).forEach(this.eventRetryQueue::add);
      if (!paramEventManifest.testing)
        this.webhookEventLogService.updateWebhookEventLogResult(((BaseEvent)paramT).id, WebhookEventResult.Succeeded); 
    } else if (!paramEventManifest.testing) {
      this.webhookEventLogService.updateWebhookEventLogResult(((BaseEvent)paramT).id, WebhookEventResult.Failed);
      throw new WebhookTransactionException();
    } 
    return list;
  }
  
  private Key signingKey(Webhook paramWebhook) {
    return paramWebhook.signatureConfiguration.enabled ? 
      (Key)this.keyCache.get(paramWebhook.signatureConfiguration.signingKeyId) : 
      null;
  }
  
  private static class EventManifest {
    public List<EventSender> senders;
    
    public boolean testing;
    
    public TransactionType transactionType;
    
    public EventManifest() {
      this.senders = new ArrayList<>(0);
    }
    
    public EventManifest(List<EventSender> param1List, TransactionType param1TransactionType, boolean param1Boolean) {
      this.senders = param1List;
      this.transactionType = param1TransactionType;
      this.testing = param1Boolean;
    }
  }
}
