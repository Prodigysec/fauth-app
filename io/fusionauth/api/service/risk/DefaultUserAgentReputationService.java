package io.fusionauth.api.service.risk;

import com.google.inject.Inject;
import io.fusionauth.api.service.cache.UserAgentReputationCache;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultUserAgentReputationService implements UserAgentReputationService {
  private static final int MAX_USER_AGENT_STRING_LENGTH = 1024;
  
  private static final Logger logger = LoggerFactory.getLogger(DefaultUserAgentReputationService.class);
  
  private final UserAgentReputationCache cache;
  
  @Inject
  public DefaultUserAgentReputationService(UserAgentReputationCache paramUserAgentReputationCache) {
    this.cache = paramUserAgentReputationCache;
  }
  
  public Optional<RiskLevel> computeRiskLevel(RiskSignalContext paramRiskSignalContext) {
    List list = (List)this.cache.get();
    if (list == null || list.isEmpty())
      return Optional.empty(); 
    if (paramRiskSignalContext == null) {
      logger.warn("The client request has no risk signal context.");
      return Optional.of(RiskLevel.NOT_AVAILABLE);
    } 
    String str = paramRiskSignalContext.userAgent;
    if (str != null && str.length() >= 1024)
      return Optional.of(RiskLevel.HIGH); 
    if (StringUtils.isBlank(str))
      return Optional.of(RiskLevel.NOT_AVAILABLE); 
    Objects.requireNonNull(str);
    boolean bool = list.stream().anyMatch(str::contains);
    return bool ? Optional.<RiskLevel>of(RiskLevel.HIGH) : Optional.<RiskLevel>of(RiskLevel.LOW);
  }
}
