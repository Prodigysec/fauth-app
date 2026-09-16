package io.fusionauth.api.service.metrics;

import com.google.inject.Inject;
import com.inversoft.license.v2.LicenseProvider;
import com.inversoft.license.v2.domain.License;
import com.inversoft.license.v2.domain.LicenseContainer;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import com.inversoft.support.service.guice.ProductVersionString;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.FusionAuthNodeMapper;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.service.count.RegistrationCountService;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.api.time.TimeUtils;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

public class MetricsSender {
  protected static final int connectTimeout = 4000;
  
  private final FusionAuthConfiguration configuration;
  
  private final LicenseProvider licenseProvider;
  
  private final LoginMapper loginMapper;
  
  private final NodeService nodeService;
  
  private final String productVersion;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  private final RegistrationCountService registrationCountService;
  
  @Inject
  public MetricsSender(FusionAuthConfiguration paramFusionAuthConfiguration, LoginMapper paramLoginMapper, NodeService paramNodeService, @ProductVersionString String paramString, ProxyInfoSupplier paramProxyInfoSupplier, RegistrationCountService paramRegistrationCountService, LicenseProvider paramLicenseProvider) {
    this.configuration = paramFusionAuthConfiguration;
    this.loginMapper = paramLoginMapper;
    this.nodeService = paramNodeService;
    this.productVersion = paramString;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
    this.registrationCountService = paramRegistrationCountService;
    this.licenseProvider = paramLicenseProvider;
  }
  
  public ClientResponse<Void, Void> send(Instance paramInstance) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    List<FusionAuthNodeMapper.FusionAuthNode> list = this.nodeService.retrieveAll();
    long l1 = this.registrationCountService.retrieveGlobalCurrentTotal();
    int i = TimeUtils.toMonth(zonedDateTime);
    long l2 = this.registrationCountService.retrieveGlobalMonthlyActiveTotal(i);
    long l3 = this.loginMapper.retrieveGlobalHourlyLoginsTotal(TimeUtils.toHour(zonedDateTime.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS)), 
        TimeUtils.toHour(zonedDateTime.truncatedTo(ChronoUnit.HOURS)));
    FormDataBodyHandler formDataBodyHandler = (new FormDataBodyHandler()).withParameter("metric.totalUsers", List.of(Long.toString(l1))).withParameter("metric.totalMonthlyLogins", List.of(Long.toString(l3))).withParameter("metric.monthlyActiveUsers", List.of(Long.toString(l2))).withParameter("metric.numberOfNodes", List.of(Integer.toString(list.size()))).withParameter("metric.productVersion", List.of(this.productVersion)).withParameter("metric.version", List.of("1"));
    Optional.<LicenseContainer>ofNullable(this.licenseProvider.getLicense())
      .map(LicenseContainer::license)
      .map(paramLicense -> paramLicense.id)
      .ifPresent(paramString -> paramFormDataBodyHandler.withParameter("licenseId", List.of(paramString)));
    return (new RESTClient(void.class, void.class))
      .url(this.configuration.metricsBaseURL())
      .uri("/api/metric")
      .urlSegment(paramInstance.id)
      
      .header("Connection", "close")
      .proxy((ProxyInfo)this.proxyInfoSupplier.get())
      .bodyHandler((RESTClient.BodyHandler)formDataBodyHandler)
      .readTimeout(4000)
      .connectTimeout(4000)
      .post()
      .go();
  }
}
