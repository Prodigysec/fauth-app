package io.fusionauth.app.action.admin.reactor;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.ReactorMetricsResponse;
import io.fusionauth.domain.api.ReactorResponse;
import io.fusionauth.domain.reactor.BreachedPasswordTenantMetric;
import io.fusionauth.domain.reactor.MFATenantMetric;
import io.fusionauth.domain.reactor.ReactorMetrics;
import io.fusionauth.domain.reactor.ReactorStatus;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "reactor_manager"})
public class IndexAction extends BaseAction {
  @FTLVariable
  public boolean allowSubClaimOverride;
  
  @FTLVariable
  public boolean licensed;
  
  @FTLVariable
  public ReactorMetrics metrics = new ReactorMetrics();
  
  @FTLVariable
  public ReactorStatus status = new ReactorStatus();
  
  @FTLVariable
  public int totalActionRequired;
  
  @FTLVariable
  public long totalMFAChallenges;
  
  @FTLVariable
  public long totalMFAFailed;
  
  @FTLVariable
  public long totalMFASucceeded;
  
  @FTLVariable
  public int totalPasswordsBreached;
  
  @FTLVariable
  public int totalPasswordsChecked;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.allowSubClaimOverride = this.frontEndSupport.configuration.allowSubClaimOverride();
    ClientResponse<ReactorResponse, Void> clientResponse = this.superClient.retrieveReactorStatus();
    if (clientResponse.wasSuccessful())
      this.status = ((ReactorResponse)clientResponse.successResponse).status; 
    ClientResponse<ReactorMetricsResponse, Void> clientResponse1 = this.superClient.retrieveReactorMetrics();
    if (clientResponse1.wasSuccessful()) {
      this.metrics = ((ReactorMetricsResponse)clientResponse1.successResponse).metrics;
      this.totalActionRequired = this.metrics.breachedPasswordMetrics.values().stream().mapToInt(paramBreachedPasswordTenantMetric -> paramBreachedPasswordTenantMetric.actionRequired).sum();
      this.totalPasswordsBreached = this.metrics.breachedPasswordMetrics.values().stream().mapToInt(BreachedPasswordTenantMetric::totalBreached).sum();
      this.totalPasswordsChecked = this.metrics.breachedPasswordMetrics.values().stream().mapToInt(paramBreachedPasswordTenantMetric -> paramBreachedPasswordTenantMetric.passwordsCheckedCount).sum();
      this.totalMFAChallenges = this.metrics.mfaMetrics.values().stream().mapToLong(paramMFATenantMetric -> paramMFATenantMetric.challengeCount).sum();
      this.totalMFAFailed = this.metrics.mfaMetrics.values().stream().mapToLong(paramMFATenantMetric -> paramMFATenantMetric.failedAttemptCount).sum();
      this.totalMFASucceeded = this.metrics.mfaMetrics.values().stream().mapToLong(paramMFATenantMetric -> paramMFATenantMetric.successCount).sum();
    } 
    return "input";
  }
  
  protected boolean showAnnouncement() {
    return false;
  }
}
