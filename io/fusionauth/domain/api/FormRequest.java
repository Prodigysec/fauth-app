package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.form.Form;

public class FormRequest {
  public Form form;
  
  @JacksonConstructor
  public FormRequest() {}
  
  public FormRequest(Form paramForm) {
    this.form = paramForm;
  }
}
