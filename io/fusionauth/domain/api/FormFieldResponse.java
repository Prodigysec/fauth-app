package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.form.FormField;
import java.util.List;

public class FormFieldResponse {
  public FormField field;
  
  public List<FormField> fields;
  
  @JacksonConstructor
  public FormFieldResponse() {}
  
  public FormFieldResponse(FormField paramFormField) {
    this.field = paramFormField;
  }
  
  public FormFieldResponse(List<FormField> paramList) {
    this.fields = paramList;
  }
}
