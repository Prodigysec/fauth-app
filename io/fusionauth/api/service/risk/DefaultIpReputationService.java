package io.fusionauth.api.service.risk;

import com.google.inject.Inject;
import com.inversoft.validator.IPAddressType;
import com.inversoft.validator.IPValidator;
import io.fusionauth.api.service.cache.IpReputationCache;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultIpReputationService implements IpReputationService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultIpReputationService.class);
  
  private final IpReputationCache cache;
  
  @Inject
  public DefaultIpReputationService(IpReputationCache paramIpReputationCache) {
    this.cache = paramIpReputationCache;
  }
  
  public Optional<RiskLevel> computeRiskLevel(RiskSignalContext paramRiskSignalContext) {
    Optional<RiskLevel> optional = Optional.of(RiskLevel.NOT_AVAILABLE);
    if (paramRiskSignalContext == null) {
      logger.error("Unable to compute risk score for ip, risk signal context is null.");
      return optional;
    } 
    if (StringUtils.isEmpty(paramRiskSignalContext.ipAddress)) {
      logger.warn("Unable to compute risk score for ip, ip is missing.");
      return optional;
    } 
    Map map = (Map)this.cache.get();
    if (map == null)
      return Optional.empty(); 
    try {
      IPAddressType iPAddressType = IPValidator.getType(paramRiskSignalContext.ipAddress);
      if (iPAddressType == IPAddressType.Unknown)
        return optional; 
      InetAddress inetAddress = InetAddress.getByName(paramRiskSignalContext.ipAddress);
      if (map.containsKey(inetAddress))
        return Optional.of(RiskLevel.HIGH); 
    } catch (UnknownHostException unknownHostException) {
      logger.error("Unable to resolve the ip address provided [{}].", paramRiskSignalContext.ipAddress);
      return optional;
    } 
    return Optional.of(RiskLevel.LOW);
  }
}
