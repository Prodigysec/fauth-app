package io.fusionauth.app.action.ajax;

import com.google.inject.Inject;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.api.ApplicationResponse;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager", "user_manager", "user_support_manager", "user_support_viewer"})
public class ApplicationAction extends BaseAJAXAction {
  public List<Application> applications;
  
  @Inject
  public ApplicationAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.applications = (List<Application>)Objects.requireNonNullElseGet(((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveApplications)).applications, Collections::emptyList);
    this.applications.sort(Comparator.comparing(paramApplication -> paramApplication.name));
    return "render";
  }
}
