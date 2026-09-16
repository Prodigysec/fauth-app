package io.fusionauth.app.action.admin.theme;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.FormResponse;
import io.fusionauth.domain.api.ThemeResponse;
import io.fusionauth.domain.form.Form;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;

@Action(value = "{themeId}", requiresAuthentication = true, constraints = {"admin", "theme_manager"})
@Forward(code = "missing", page = "/errors/404.ftl", status = 200, cacheControl = "no-store")
public class ViewAction extends BaseViewAction {
  public List<Application> applications = new ArrayList<>();
  
  public Theme theme;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.theme = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTheme(this.themeId))).theme;
    this.applications.addAll(((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveApplications)).applications);
    this.applications.sort(Comparator.comparing(paramApplication -> paramApplication.name));
    if (this.applicationId == null) {
      this.application = this.applications.get(0);
      this.applicationId = this.application.id;
    } else {
      this.application = this.applications.stream().filter(paramApplication -> paramApplication.id.equals(this.applicationId)).findFirst().orElse(null);
    } 
    if (this.application != null) {
      this.client_id = this.application.oauthConfiguration.clientId;
      this.redirect_uri = this.application.oauthConfiguration.getFirstAuthorizedRedirectURLIgnoringPatterns();
      if (this.application.registrationConfiguration.enabled && this.application.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.advanced) {
        Form form = ((FormResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveForm(this.application.registrationConfiguration.formId))).form;
        this.totalSteps = (form != null) ? form.steps.size() : 0;
      } 
    } 
    return "input";
  }
}
