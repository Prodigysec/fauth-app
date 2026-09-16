package io.fusionauth.domain.form;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.util.Normalizer;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class FormField implements Buildable<FormField>, JSONColumnable {
  @JSONColumn
  public boolean confirm;
  
  public UUID consentId;
  
  @JSONColumn
  public FormControl control;
  
  public Map<String, Object> data = new LinkedHashMap<>();
  
  @JSONColumn
  public String description;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  @JSONColumn
  public String key;
  
  public ZonedDateTime lastUpdateInstant;
  
  public String name;
  
  @JSONColumn
  public List<String> options = new ArrayList<>();
  
  @JSONColumn
  public boolean required;
  
  @JSONColumn
  public FormDataType type = FormDataType.string;
  
  @JSONColumn
  public FormFieldValidator validator = new FormFieldValidator();
  
  public FormField() {}
  
  public FormField(FormField paramFormField) {
    this.confirm = paramFormField.confirm;
    this.consentId = paramFormField.consentId;
    this.control = paramFormField.control;
    this.data = new LinkedHashMap<>(paramFormField.data);
    this.description = paramFormField.description;
    this.id = paramFormField.id;
    this.insertInstant = paramFormField.insertInstant;
    this.key = paramFormField.key;
    this.lastUpdateInstant = paramFormField.lastUpdateInstant;
    this.name = paramFormField.name;
    this.options = new ArrayList<>(paramFormField.options);
    this.required = paramFormField.required;
    this.type = paramFormField.type;
    if (paramFormField.validator != null) {
      this.validator = new FormFieldValidator();
      this.validator.expression = paramFormField.validator.expression;
      this.validator.enabled = paramFormField.validator.enabled;
    } 
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof FormField))
      return false; 
    FormField formField = (FormField)paramObject;
    return (this.confirm == formField.confirm && this.required == formField.required && 
      
      Objects.equals(this.consentId, formField.consentId) && this.control == formField.control && 
      
      Objects.equals(this.data, formField.data) && 
      Objects.equals(this.description, formField.description) && 
      Objects.equals(this.id, formField.id) && 
      Objects.equals(this.insertInstant, formField.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, formField.lastUpdateInstant) && 
      Objects.equals(this.key, formField.key) && 
      Objects.equals(this.name, formField.name) && 
      Objects.equals(this.options, formField.options) && this.type == formField.type && 
      
      Objects.equals(this.validator, formField.validator));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          Boolean.valueOf(this.confirm), this.consentId, this.control, this.data, this.description, this.id, this.insertInstant, this.lastUpdateInstant, this.key, this.name, 
          this.options, Boolean.valueOf(this.required), this.type, this.validator });
  }
  
  public void normalize() {
    Normalizer.removeEmpty(this.options);
    if (this.control == FormControl.checkbox && this.type == FormDataType.bool) {
      Normalizer.toLowerCase(this.options, ArrayList::new);
      this.options.sort(Collections.reverseOrder());
    } 
    FormField formField = ManagedFields.Values.get(this.key);
    if (this.control == null && this.key != null)
      this.control = (formField == null) ? FormControl.text : formField.control; 
    if (formField != null) {
      if (this.key.equals("user.password")) {
        this.control = FormControl.password;
        this.required = true;
      } 
      this.type = formField.type;
    } 
    if (this.control != FormControl.number && this.control != FormControl.text && this.control != FormControl.textarea) {
      this.validator.enabled = false;
      this.validator.expression = null;
    } 
    if (this.type == FormDataType.email) {
      this.control = FormControl.text;
    } else if (this.type == FormDataType.consent) {
      this.confirm = false;
      this.control = null;
      this.key = "consents['" + String.valueOf(this.consentId) + "']";
    } 
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
