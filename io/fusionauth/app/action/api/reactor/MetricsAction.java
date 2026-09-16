package io.fusionauth.app.action.api.reactor;

import com.google.inject.Inject;
import io.fusionauth.api.service.reactor.ReactorMetricsService;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.ReactorMetricsResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class MetricsAction extends BaseAPIAction {
  private final ReactorMetricsService reactorMetricsService;
  
  @JSONResponse
  public ReactorMetricsResponse response;
  
  @Inject
  public MetricsAction(FrontEndSupport paramFrontEndSupport, ReactorMetricsService paramReactorMetricsService) {
    super(paramFrontEndSupport);
    this.reactorMetricsService = paramReactorMetricsService;
  }
  
  public String get() {
    this.response = new ReactorMetricsResponse();
    this.response.metrics = this.reactorMetricsService.retrieveMetrics();
    return "render";
  }
}
