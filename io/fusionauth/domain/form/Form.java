package io.fusionauth.domain.form;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.util.Normalizer;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Form implements Buildable<Form>, JSONColumnable {
  public Map<String, Object> data = new LinkedHashMap<>();
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public String name;
  
  public List<FormStep> steps = new ArrayList<>();
  
  public FormType type = FormType.registration;
  
  public Form(Form paramForm) {
    this.data.putAll(paramForm.data);
    this.id = paramForm.id;
    this.insertInstant = paramForm.insertInstant;
    this.lastUpdateInstant = paramForm.lastUpdateInstant;
    this.name = paramForm.name;
    paramForm.steps.forEach(paramFormStep -> this.steps.add(new FormStep(paramFormStep)));
    this.type = paramForm.type;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Form))
      return false; 
    Form form = (Form)paramObject;
    return (Objects.equals(this.data, form.data) && 
      Objects.equals(this.id, form.id) && 
      Objects.equals(this.insertInstant, form.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, form.lastUpdateInstant) && 
      Objects.equals(this.name, form.name) && 
      Objects.equals(this.steps, form.steps) && this.type == form.type);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.data, this.id, this.insertInstant, this.lastUpdateInstant, this.name, this.steps, this.type });
  }
  
  public void normalize() {
    Normalizer.removeEmpty(this.data);
    Normalizer.removeEmpty(this.steps);
    this.steps.removeIf(paramFormStep -> (paramFormStep.type == FormStepType.collectData && paramFormStep.fields.isEmpty()));
    this.steps.stream().map(paramFormStep -> paramFormStep.fields).forEach(Normalizer::removeEmpty);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  @JacksonConstructor
  public Form() {}
}
