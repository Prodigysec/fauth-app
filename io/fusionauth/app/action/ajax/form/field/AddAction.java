package io.fusionauth.app.action.ajax.form.field;

import com.google.inject.Inject;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.FormFieldResponse;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "form_manager"})
public class AddAction extends BaseAJAXAction {
  public UUID fieldId;
  
  @FTLVariable
  public List<FormField> fields = new ArrayList<>();
  
  @FTLVariable
  public int step;
  
  @FTLVariable
  public FormType type;
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    return "success";
  }
  
  @FormPrepareMethod
  public void prepare() {
    List<FormField> list = ((FormFieldResponse)superDelegate().execute(FusionAuthClient::retrieveFormFields)).fields;
    switch (this.type) {
      case adminRegistration:
        this.fields.addAll((Collection<? extends FormField>)list.stream().filter(paramFormField -> paramFormField.key.startsWith("registration.")).collect(Collectors.toList()));
        return;
      case adminUser:
        this.fields.addAll((Collection<? extends FormField>)list.stream().filter(paramFormField -> paramFormField.key.startsWith("user.")).collect(Collectors.toList()));
        return;
    } 
    this.fields.addAll(list);
  }
}
