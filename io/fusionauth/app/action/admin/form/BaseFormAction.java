package io.fusionauth.app.action.admin.form;

import com.inversoft.error.Error;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.FormFieldResponse;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

public abstract class BaseFormAction extends BaseAction {
  @FTLVariable
  public static final FormType[] formTypes = FormType.values();
  
  @FTLVariable
  public Map<UUID, FormField> fields = new HashMap<>();
  
  public Form form = new Form();
  
  public UUID formId;
  
  @FTLVariable
  public boolean verifyEmailIncluded;
  
  @FTLVariable
  public boolean verifyPhoneIncluded;
  
  public BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    paramFrontEndSupport.errorMapperFunction = (paramError -> paramError);
    paramFrontEndSupport.fieldMapperFunction = (paramString -> paramString.replaceAll("form\\.steps\\[\\d+]\\.fields\\[\\d+]", "form.steps.fields").replaceAll("form\\.steps\\[\\d+]\\.fields", "form.steps.fields").replaceAll("form\\.steps\\[\\d+]\\.type", "form.steps"));
  }
  
  @FormPrepareMethod
  public void prepare() {
    ((List)Objects.requireNonNullElseGet(((FormFieldResponse)superDelegate().execute(FusionAuthClient::retrieveFormFields)).fields, Collections::emptyList))
      .forEach(paramObject -> this.fields.put(((FormField)paramObject).id, (FormField)paramObject));
  }
}
