package io.fusionauth.api.service.risk;

public interface RiskSignalService {
  CompositeRisk computeClientRisk(RiskSignalContext paramRiskSignalContext);
}
