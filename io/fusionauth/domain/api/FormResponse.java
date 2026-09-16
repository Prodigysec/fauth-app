package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.form.Form;
import java.util.List;

public class FormResponse {
  public Form form;
  
  public List<Form> forms;
  
  @JacksonConstructor
  public FormResponse() {}
  
  public FormResponse(Form paramForm) {
    this.form = paramForm;
  }
  
  public FormResponse(List<Form> paramList) {
    this.forms = paramList;
  }
}
