package io.fusionauth.api.service.reactor;

import com.google.inject.Inject;
import io.fusionauth.api.domain.BreachedPasswordMapper;
import io.fusionauth.api.domain.MFAMetricsMapper;
import io.fusionauth.api.domain.mybatis._BreachedPasswordTenantMetric;
import io.fusionauth.api.domain.mybatis._MFATenantMetric;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.domain.reactor.BreachedPasswordTenantMetric;
import io.fusionauth.domain.reactor.MFATenantMetric;
import io.fusionauth.domain.reactor.ReactorMetrics;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DefaultReactorMetricsService implements ReactorMetricsService {
  private final BreachedPasswordMapper breachedPasswordMapper;
  
  private final MFAMetricsMapper mfaMetricsMapper;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultReactorMetricsService(BreachedPasswordMapper paramBreachedPasswordMapper, MFAMetricsMapper paramMFAMetricsMapper, UserReaderService paramUserReaderService) {
    this.breachedPasswordMapper = paramBreachedPasswordMapper;
    this.mfaMetricsMapper = paramMFAMetricsMapper;
    this.userReader = paramUserReaderService;
  }
  
  public ReactorMetrics retrieveMetrics() {
    ReactorMetrics reactorMetrics = new ReactorMetrics();
    List<_BreachedPasswordTenantMetric> list = this.breachedPasswordMapper.retrieveAllBreachedMetrics();
    if (list != null) {
      reactorMetrics.breachedPasswordMetrics = (Map<UUID, BreachedPasswordTenantMetric>)list.stream().collect(Collectors.toMap(param_BreachedPasswordTenantMetric -> param_BreachedPasswordTenantMetric.tenantId, param_BreachedPasswordTenantMetric -> param_BreachedPasswordTenantMetric));
      reactorMetrics.breachedPasswordMetrics.forEach((paramUUID, paramBreachedPasswordTenantMetric) -> paramBreachedPasswordTenantMetric.actionRequired = (int)(paramBreachedPasswordTenantMetric.actionRequired + this.userReader.retrieveBreachedUsersRequiringActionCountByTenantId(paramUUID)));
    } 
    List<_MFATenantMetric> list1 = this.mfaMetricsMapper.retrieveAllMfaMetrics();
    if (list1 != null)
      reactorMetrics.mfaMetrics = (Map<UUID, MFATenantMetric>)list1.stream().collect(Collectors.toMap(param_MFATenantMetric -> param_MFATenantMetric.tenantId, param_MFATenantMetric -> param_MFATenantMetric)); 
    return reactorMetrics;
  }
}
