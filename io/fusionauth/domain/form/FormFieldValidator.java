package io.fusionauth.domain.form;

import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Enableable;
import java.util.Objects;

public class FormFieldValidator extends Enableable implements Buildable<FormFieldValidator> {
  public String expression;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof FormFieldValidator))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    FormFieldValidator formFieldValidator = (FormFieldValidator)paramObject;
    return Objects.equals(this.expression, formFieldValidator.expression);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.expression });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
