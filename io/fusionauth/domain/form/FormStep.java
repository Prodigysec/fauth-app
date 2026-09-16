package io.fusionauth.domain.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.api.domain.annotation.InternalUse;
import io.fusionauth.domain.Buildable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FormStep implements Buildable<FormStep> {
  @InternalUse
  @JsonIgnore
  public List<FormField> fieldObjects = new ArrayList<>();
  
  public List<UUID> fields = new ArrayList<>();
  
  public FormStepType type = FormStepType.collectData;
  
  @JacksonConstructor
  public FormStep() {}
  
  public FormStep(FormStep paramFormStep) {
    this.type = paramFormStep.type;
    this.fields.addAll(paramFormStep.fields);
  }
  
  public FormStep(UUID... paramVarArgs) {
    this.fields.addAll(Arrays.asList(paramVarArgs));
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof FormStep))
      return false; 
    FormStep formStep = (FormStep)paramObject;
    return (Objects.equals(this.fields, formStep.fields) && this.type == formStep.type);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.fields, this.type });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
