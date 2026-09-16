package io.fusionauth.api.service.risk;

import java.util.Optional;

public interface UserAgentReputationService {
  Optional<RiskLevel> computeRiskLevel(RiskSignalContext paramRiskSignalContext);
}
