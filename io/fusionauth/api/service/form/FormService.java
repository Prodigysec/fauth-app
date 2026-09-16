package io.fusionauth.api.service.form;

import com.inversoft.util.CollectionTools;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormControl;
import io.fusionauth.domain.form.FormField;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FormService {
  public static final Set<FormControl> RequireOptions = CollectionTools.set((Object[])new FormControl[] { FormControl.radio, FormControl.checkbox, FormControl.select });
  
  void create(Form paramForm);
  
  void createField(FormField paramFormField);
  
  void delete(Form paramForm);
  
  void deleteField(FormField paramFormField);
  
  List<Form> retrieveAll();
  
  List<FormField> retrieveAllFields();
  
  Form retrieveById(UUID paramUUID);
  
  FormField retrieveFieldById(UUID paramUUID);
  
  List<FormField> retrieveFieldsByFormId(UUID paramUUID);
  
  void update(Form paramForm1, Form paramForm2);
  
  void updateField(FormField paramFormField1, FormField paramFormField2);
  
  ValidationResult validateCreate(Form paramForm);
  
  ValidationResult validateDelete(UUID paramUUID);
  
  FieldValidationResult validateFieldCreate(FormField paramFormField);
  
  FieldValidationResult validateFieldDelete(UUID paramUUID);
  
  FieldValidationResult validateFieldUpdate(FormField paramFormField);
  
  ValidationResult validateUpdate(Form paramForm);
  
  public static class FieldValidationResult extends BaseValidationResult {
    public FormField existing;
    
    public FormField field;
  }
  
  public static class ValidationResult extends BaseValidationResult {
    public Form existing;
    
    public Form form;
  }
}
