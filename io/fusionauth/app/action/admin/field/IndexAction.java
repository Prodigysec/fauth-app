package io.fusionauth.app.action.admin.field;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.FormFieldResponse;
import io.fusionauth.domain.form.FormField;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "form_manager"})
public class IndexAction extends BaseAction {
  public List<FormField> fields;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.fields = (List<FormField>)Objects.requireNonNullElseGet(((FormFieldResponse)superDelegate().execute(FusionAuthClient::retrieveFormFields)).fields, Collections::emptyList);
    return "input";
  }
}
