package io.fusionauth.app.action.admin.group;

import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.api.ApplicationResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

public abstract class BaseFormAction extends BaseAction {
  public List<Application> applications = new ArrayList<>();
  
  public UUID groupId;
  
  public List<UUID> roleIds;
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @FormPrepareMethod
  public void retrieveApplications() {
    this.applications.addAll((Collection<? extends Application>)Objects.requireNonNullElseGet(((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveApplications)).applications, Collections::emptyList));
    this.applications.sort(Comparator.comparing(paramApplication -> paramApplication.name));
  }
}
