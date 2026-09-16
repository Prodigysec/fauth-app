package io.fusionauth.api.service.risk;

import java.util.Optional;

public interface IpReputationService {
  Optional<RiskLevel> computeRiskLevel(RiskSignalContext paramRiskSignalContext);
}
