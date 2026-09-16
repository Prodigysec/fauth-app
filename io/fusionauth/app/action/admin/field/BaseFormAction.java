package io.fusionauth.app.action.admin.field;

import com.inversoft.error.Error;
import com.inversoft.util.CollectionTools;
import com.inversoft.util.Pair;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.api.ConsentResponse;
import io.fusionauth.domain.form.FormControl;
import io.fusionauth.domain.form.FormDataType;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.ManagedFields;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

public abstract class BaseFormAction extends BaseAction {
  @FTLVariable
  public static List<Pair<String, String>> userFields;
  
  @FTLVariable
  public List<Consent> consents;
  
  @FTLVariable
  public FormControl[] controls = new FormControl[] { FormControl.text, FormControl.password, FormControl.checkbox, FormControl.number, FormControl.radio, FormControl.select, FormControl.textarea };
  
  public FormField field = new FormField();
  
  public UUID fieldId;
  
  public String options;
  
  @FTLVariable
  public FormDataType[] types = new FormDataType[] { FormDataType.string, FormDataType.email, FormDataType.phoneNumber, FormDataType.number, FormDataType.date, FormDataType.bool };
  
  public BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    paramFrontEndSupport.errorMapperFunction = (paramError -> paramError);
    paramFrontEndSupport.fieldMapperFunction = (paramString -> paramString.replace("field.options", "options"));
  }
  
  @FormPrepareMethod
  public void prepareForm() {
    this.consents = (List<Consent>)Objects.requireNonNullElseGet(((ConsentResponse)this.delegate.execute(FusionAuthClient::retrieveConsents)).consents, Collections::emptyList);
  }
  
  protected void validateFieldOptions() {
    Collection<? extends String> collection = CollectionTools.stringToCollection(this.options);
    if (collection == null) {
      this.frontEndSupport.addFieldError("options", "[invalid]options", new Object[0]);
    } else {
      this.field.options.addAll(collection);
    } 
  }
  
  static {
    userFields = (List<Pair<String, String>>)ManagedFields.Values.keySet().stream().map(paramString -> new Pair(paramString, paramString)).collect(Collectors.toList());
    userFields.add(new Pair("consents.", "consents."));
    userFields.add(new Pair("registration.data.", "registration.data."));
    userFields.add(new Pair("user.data.", "user.data."));
  }
}
