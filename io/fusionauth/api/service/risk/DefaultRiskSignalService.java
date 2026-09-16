package io.fusionauth.api.service.risk;

import com.google.inject.Inject;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.ZonedDateTimeWrapper;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.domain.AuthenticationThreats;
import io.fusionauth.domain.ClientRiskConfiguration;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultRiskSignalService implements RiskSignalService {
  private final ImpossibleTravelService impossibleTravelService;
  
  private final IpReputationService ipReputationService;
  
  private final ReactorStatusService reactorStatusService;
  
  private final UserAgentReputationService userAgentReputationService;
  
  @Inject
  public DefaultRiskSignalService(ImpossibleTravelService paramImpossibleTravelService, IpReputationService paramIpReputationService, ReactorStatusService paramReactorStatusService, UserAgentReputationService paramUserAgentReputationService) {
    this.impossibleTravelService = paramImpossibleTravelService;
    this.ipReputationService = paramIpReputationService;
    this.reactorStatusService = paramReactorStatusService;
    this.userAgentReputationService = paramUserAgentReputationService;
  }
  
  private static Optional<RiskLevel> computeBotDetectionScore(RiskSignalContext paramRiskSignalContext) {
    Double double_ = paramRiskSignalContext.botDetectionScore;
    if (double_ == null || double_.doubleValue() < 0.0D || double_.doubleValue() > 1.0D)
      return Optional.empty(); 
    return Optional.of((double_.doubleValue() <= 0.5D) ? RiskLevel.LOW : RiskLevel.HIGH);
  }
  
  private static RiskLevel computeDormantAccount(RiskSignalContext paramRiskSignalContext, ZonedDateTime paramZonedDateTime) {
    ZonedDateTime zonedDateTime = paramRiskSignalContext.user.lastLoginInstant;
    if (zonedDateTime == null)
      return RiskLevel.LOW; 
    long l = ChronoUnit.DAYS.between(zonedDateTime, paramZonedDateTime);
    if (l < 30L)
      return RiskLevel.LOW; 
    if (l < 180L)
      return RiskLevel.MEDIUM; 
    return RiskLevel.HIGH;
  }
  
  private static RiskLevel computeDormantPassword(RiskSignalContext paramRiskSignalContext, ZonedDateTime paramZonedDateTime) {
    ZonedDateTime zonedDateTime = paramRiskSignalContext.user.passwordLastUpdateInstant;
    if (zonedDateTime == null)
      return RiskLevel.NOT_AVAILABLE; 
    long l = ChronoUnit.DAYS.between(zonedDateTime, paramZonedDateTime);
    if (l < 90L)
      return RiskLevel.LOW; 
    if (l < 180L)
      return RiskLevel.MEDIUM; 
    return RiskLevel.HIGH;
  }
  
  private static RiskLevel computeRecentIdentityChange(RiskSignalContext paramRiskSignalContext, ZonedDateTime paramZonedDateTime) {
    User user = paramRiskSignalContext.user;
    if (user.identities.isEmpty())
      return RiskLevel.NOT_AVAILABLE; 
    return user.identities.stream()
      .map(paramUserIdentity -> paramUserIdentity.lastUpdateInstant)
      .filter(Objects::nonNull)
      .max(Comparator.naturalOrder())
      .map(paramZonedDateTime2 -> {
          long l = ChronoUnit.DAYS.between(paramZonedDateTime2, paramZonedDateTime1);
          return (l < 1L) ? RiskLevel.HIGH : ((l < 7L) ? RiskLevel.MEDIUM : RiskLevel.LOW);
        }).orElse(RiskLevel.NOT_AVAILABLE);
  }
  
  private static RiskLevel computeRecentPasswordChange(RiskSignalContext paramRiskSignalContext, ZonedDateTime paramZonedDateTime) {
    ZonedDateTime zonedDateTime = paramRiskSignalContext.user.passwordLastUpdateInstant;
    if (zonedDateTime == null)
      return RiskLevel.NOT_AVAILABLE; 
    long l = ChronoUnit.DAYS.between(zonedDateTime, paramZonedDateTime);
    if (l < 1L)
      return RiskLevel.HIGH; 
    if (l >= 7L)
      return RiskLevel.LOW; 
    return RiskLevel.MEDIUM;
  }
  
  private static RiskLevel computeUntrustedDevice(RiskSignalContext paramRiskSignalContext) {
    ExternalIdentifier externalIdentifier = paramRiskSignalContext.mfaTrust;
    User user = paramRiskSignalContext.user;
    boolean bool = (externalIdentifier != null && externalIdentifier.type == ExternalIdentifier.ExternalIdType.TwoFactorTrust && externalIdentifier.userId != null && externalIdentifier.userId.equals(user.id)) ? true : false;
    return bool ? RiskLevel.LOW : RiskLevel.HIGH;
  }
  
  private static Set<AuthenticationThreats> getSignalTypes(ClientRiskConfiguration paramClientRiskConfiguration, ReactorStatus paramReactorStatus) {
    boolean bool = (paramClientRiskConfiguration != null && paramClientRiskConfiguration.enabled && ReactorStatusValidator.isLicensedFor(paramReactorStatus, paramReactorStatus -> paramReactorStatus.clientRiskConfiguration)) ? true : false;
    return (Set<AuthenticationThreats>)Stream.<AuthenticationThreats>of(AuthenticationThreats.values())
      .filter(paramAuthenticationThreats -> isLicensedAndAvailable(paramAuthenticationThreats, paramReactorStatus))
      .filter(paramAuthenticationThreats -> (!paramBoolean || isSignalEnabled(paramAuthenticationThreats, paramClientRiskConfiguration)))
      .collect(Collectors.toSet());
  }
  
  private static boolean isLicensedAndAvailable(AuthenticationThreats paramAuthenticationThreats, ReactorStatus paramReactorStatus) {
    switch (paramAuthenticationThreats) {
      case ImpossibleTravel:
      
      case BlocklistedIp:
      
      case SuspiciousUserAgent:
      
    } 
    return true;
  }
  
  private static boolean isSignalEnabled(AuthenticationThreats paramAuthenticationThreats, ClientRiskConfiguration paramClientRiskConfiguration) {
    switch (paramAuthenticationThreats) {
      default:
        throw new MatchException(null, null);
      case BotDetected:
      
      case DormantAccount:
      
      case ImpossibleTravel:
      
      case BlocklistedIp:
      
      case UnrecognizedDevice:
      
      case RecentIdentityChange:
      
      case DormantPassword:
      
      case RecentPasswordChange:
      
      case UntrustedDevice:
      
      case SuspiciousUserAgent:
        break;
    } 
    return 








      
      paramClientRiskConfiguration.suspiciousUserAgent;
  }
  
  public CompositeRisk computeClientRisk(RiskSignalContext paramRiskSignalContext) {
    ZonedDateTime zonedDateTime = ZonedDateTimeWrapper.now(ZoneOffset.UTC);
    ReactorStatus reactorStatus = this.reactorStatusService.retrieveStatus();
    Set<AuthenticationThreats> set = getSignalTypes(paramRiskSignalContext.clientRiskConfiguration, reactorStatus);
    ArrayList<RiskSignal> arrayList = new ArrayList();
    if (set.contains(AuthenticationThreats.DormantAccount))
      arrayList.add(new RiskSignal(AuthenticationThreats.DormantAccount, computeDormantAccount(paramRiskSignalContext, zonedDateTime))); 
    if (set.contains(AuthenticationThreats.RecentIdentityChange))
      arrayList.add(new RiskSignal(AuthenticationThreats.RecentIdentityChange, computeRecentIdentityChange(paramRiskSignalContext, zonedDateTime))); 
    if (set.contains(AuthenticationThreats.DormantPassword))
      arrayList.add(new RiskSignal(AuthenticationThreats.DormantPassword, computeDormantPassword(paramRiskSignalContext, zonedDateTime))); 
    if (set.contains(AuthenticationThreats.RecentPasswordChange))
      arrayList.add(new RiskSignal(AuthenticationThreats.RecentPasswordChange, computeRecentPasswordChange(paramRiskSignalContext, zonedDateTime))); 
    if (set.contains(AuthenticationThreats.BlocklistedIp)) {
      Optional<RiskLevel> optional = this.ipReputationService.computeRiskLevel(paramRiskSignalContext);
      optional.ifPresent(paramRiskLevel -> paramList.add(new RiskSignal(AuthenticationThreats.BlocklistedIp, paramRiskLevel)));
    } 
    if (set.contains(AuthenticationThreats.SuspiciousUserAgent)) {
      Optional<RiskLevel> optional = this.userAgentReputationService.computeRiskLevel(paramRiskSignalContext);
      optional.ifPresent(paramRiskLevel -> paramList.add(new RiskSignal(AuthenticationThreats.SuspiciousUserAgent, paramRiskLevel)));
    } 
    if (set.contains(AuthenticationThreats.BotDetected)) {
      Optional<RiskLevel> optional = computeBotDetectionScore(paramRiskSignalContext);
      optional.ifPresent(paramRiskLevel -> paramList.add(new RiskSignal(AuthenticationThreats.BotDetected, paramRiskLevel)));
    } 
    if (set.contains(AuthenticationThreats.UnrecognizedDevice))
      arrayList.add(new RiskSignal(AuthenticationThreats.UnrecognizedDevice, computeUnrecognizedDevice(paramRiskSignalContext))); 
    if (set.contains(AuthenticationThreats.UntrustedDevice))
      arrayList.add(new RiskSignal(AuthenticationThreats.UntrustedDevice, computeUntrustedDevice(paramRiskSignalContext))); 
    if (set.contains(AuthenticationThreats.ImpossibleTravel)) {
      Optional<RiskLevel> optional = this.impossibleTravelService.computeRiskLevel(paramRiskSignalContext);
      optional.ifPresent(paramRiskLevel -> paramList.add(new RiskSignal(AuthenticationThreats.ImpossibleTravel, paramRiskLevel)));
    } 
    return arrayList.isEmpty() ? 
      new CompositeRisk(RiskLevel.HIGH, new EnumMap<>(AuthenticationThreats.class), false) : 
      CompositeRisk.compute(arrayList);
  }
  
  private RiskLevel computeUnrecognizedDevice(RiskSignalContext paramRiskSignalContext) {
    return paramRiskSignalContext.newDevice ? RiskLevel.HIGH : RiskLevel.LOW;
  }
}
