package io.fusionauth.api.service.event;

import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.event.BaseEvent;
import io.fusionauth.domain.event.EventType;
import java.util.List;

public interface EventService {
  public static final List<EventType> BreachPasswordLicensedEvents = List.of(EventType.UserPasswordBreach);
  
  public static final List<EventType> IMFAWebhookLicensedEvents = List.of(EventType.UserTwoFactorChallenge, EventType.UserTwoFactorFailedAttempt, EventType.UserTwoFactorSuccess);
  
  public static final List<EventType> ThreatDetectionLicensedEvents = List.of(EventType.UserLoginIdDuplicateOnCreate, EventType.UserLoginIdDuplicateOnUpdate, EventType.UserLoginNewDevice, EventType.UserLoginSuspicious, EventType.UserPasswordResetSend, EventType.UserPasswordResetStart, EventType.UserPasswordResetSuccess, EventType.UserPasswordUpdate, EventType.UserTwoFactorMethodAdd, EventType.UserTwoFactorMethodRemove);
  
  void send(Tenant paramTenant, Application paramApplication, BaseEvent paramBaseEvent) throws WebhookTransactionException;
  
  EventSenderResult sendTest(BaseEvent paramBaseEvent, EventSender paramEventSender) throws WebhookTransactionException;
}
