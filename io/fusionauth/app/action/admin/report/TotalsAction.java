package io.fusionauth.app.action.admin.report;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.report.TotalsReportResponse;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "report_viewer"})
public class TotalsAction extends BaseAction {
  public Map<UUID, Application> applicationMap = new HashMap<>();
  
  public TotalsReportResponse response;
  
  @Inject
  public TotalsAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    List<Application> list = ((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveApplications)).applications;
    list.forEach(paramApplication -> this.applicationMap.put(paramApplication.id, paramApplication));
    list.sort(Comparator.comparing(paramApplication -> paramApplication.name));
    this.response = superDelegate().execute(FusionAuthClient::retrieveTotalReport);
    return "input";
  }
}
