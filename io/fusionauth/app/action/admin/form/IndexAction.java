package io.fusionauth.app.action.admin.form;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.FormResponse;
import io.fusionauth.domain.form.Form;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "form_manager"})
public class IndexAction extends BaseAction {
  public List<Form> forms;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.forms = (List<Form>)Objects.requireNonNullElseGet(((FormResponse)superDelegate().execute(FusionAuthClient::retrieveForms)).forms, Collections::emptyList);
    return "input";
  }
}
