package io.fusionauth.app.service.user;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormField;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormViewModel implements Buildable<FormViewModel> {
  public final List<FormStepViewModel> steps;
  
  public FormViewModel(Form paramForm) {
    this((List<FormStepViewModel>)paramForm.steps.stream().map(FormStepViewModel::new).collect(Collectors.toList()));
  }
  
  public FormViewModel(List<FormStepViewModel> paramList) {
    this.steps = paramList;
  }
  
  @JacksonConstructor
  private FormViewModel() {
    this.steps = null;
  }
  
  public boolean equals(Object paramObject) {
    FormViewModel formViewModel;
    if (paramObject instanceof FormViewModel) {
      formViewModel = (FormViewModel)paramObject;
    } else {
      return false;
    } 
    return Objects.equals(this.steps, formViewModel.steps);
  }
  
  public List<FormField> fieldsList() {
    return (List<FormField>)this.steps.stream()
      .flatMap(paramFormStepViewModel -> paramFormStepViewModel.fieldObjects.stream())
      .collect(Collectors.toList());
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.steps });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
