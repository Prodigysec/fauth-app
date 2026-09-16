package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.form.FormField;
import java.util.List;

public class FormFieldRequest {
  public FormField field;
  
  public List<FormField> fields;
  
  @JacksonConstructor
  public FormFieldRequest() {}
  
  public FormFieldRequest(FormField paramFormField) {
    this.field = paramFormField;
  }
  
  public FormFieldRequest(List<FormField> paramList) {
    this.fields = paramList;
  }
}
