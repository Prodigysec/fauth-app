package io.fusionauth.api.service.risk;

import java.util.Optional;

public interface ImpossibleTravelService {
  Optional<RiskLevel> computeRiskLevel(RiskSignalContext paramRiskSignalContext);
}
