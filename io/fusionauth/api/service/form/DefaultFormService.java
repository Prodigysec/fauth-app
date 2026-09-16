package io.fusionauth.api.service.form;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.ConsentMapper;
import io.fusionauth.api.domain.FormMapper;
import io.fusionauth.api.domain.TenantManagerMapper;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormControl;
import io.fusionauth.domain.form.FormDataType;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStep;
import io.fusionauth.domain.form.FormStepType;
import io.fusionauth.domain.form.FormType;
import io.fusionauth.domain.form.ManagedFields;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mybatis.guice.transactional.Transactional;

public class DefaultFormService implements FormService {
  private final ApplicationMapper applicationMapper;
  
  private final ConsentMapper consentMapper;
  
  private final FormMapper formMapper;
  
  private final ReactorStatusService reactorStatusService;
  
  private final TenantManagerMapper tenantManagerMapper;
  
  private final TenantMapper tenantMapper;
  
  @Inject
  public DefaultFormService(ApplicationMapper paramApplicationMapper, ConsentMapper paramConsentMapper, FormMapper paramFormMapper, ReactorStatusService paramReactorStatusService, TenantManagerMapper paramTenantManagerMapper, TenantMapper paramTenantMapper) {
    this.applicationMapper = paramApplicationMapper;
    this.consentMapper = paramConsentMapper;
    this.formMapper = paramFormMapper;
    this.reactorStatusService = paramReactorStatusService;
    this.tenantManagerMapper = paramTenantManagerMapper;
    this.tenantMapper = paramTenantMapper;
  }
  
  @Transactional
  public void create(Form paramForm) {
    if (paramForm.id == null)
      paramForm.id = UUID.randomUUID(); 
    paramForm.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramForm.lastUpdateInstant = paramForm.insertInstant;
    this.formMapper.create(paramForm);
    createFormSteps(paramForm);
  }
  
  public void createField(FormField paramFormField) {
    if (paramFormField.id == null)
      paramFormField.id = UUID.randomUUID(); 
    paramFormField.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramFormField.lastUpdateInstant = paramFormField.insertInstant;
    paramFormField.normalize();
    this.formMapper.createField(paramFormField);
    setSyntheticConsentFields(paramFormField);
  }
  
  @Transactional
  public void delete(Form paramForm) {
    this.formMapper.deleteFormSteps(paramForm.id);
    this.formMapper.delete(paramForm.id);
  }
  
  @Transactional
  public void deleteField(FormField paramFormField) {
    this.formMapper.deleteField(paramFormField.id);
  }
  
  public List<Form> retrieveAll() {
    return this.formMapper.retrieveAll();
  }
  
  public List<FormField> retrieveAllFields() {
    List<FormField> list = this.formMapper.retrieveAllFields();
    list.forEach(this::setSyntheticConsentFields);
    return list;
  }
  
  public Form retrieveById(UUID paramUUID) {
    Form form = this.formMapper.retrieveById(paramUUID);
    if (form != null)
      form.steps.forEach(paramFormStep -> paramFormStep.fieldObjects.forEach(this::setSyntheticConsentFields)); 
    return form;
  }
  
  public FormField retrieveFieldById(UUID paramUUID) {
    FormField formField = this.formMapper.retrieveFieldById(paramUUID);
    setSyntheticConsentFields(formField);
    return formField;
  }
  
  public List<FormField> retrieveFieldsByFormId(UUID paramUUID) {
    Form form = retrieveById(paramUUID);
    if (form == null)
      return List.of(); 
    return form.steps
      .stream()
      .flatMap(paramFormStep -> paramFormStep.fieldObjects.stream())
      .toList();
  }
  
  @Transactional
  public void update(Form paramForm1, Form paramForm2) {
    paramForm2.id = paramForm1.id;
    paramForm2.insertInstant = paramForm1.insertInstant;
    paramForm2.type = paramForm1.type;
    paramForm2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.formMapper.update(paramForm2);
    this.formMapper.deleteFormSteps(paramForm2.id);
    createFormSteps(paramForm2);
  }
  
  public void updateField(FormField paramFormField1, FormField paramFormField2) {
    paramFormField2.type = paramFormField1.type;
    paramFormField2.key = paramFormField1.key;
    paramFormField2.insertInstant = paramFormField1.insertInstant;
    paramFormField2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    if (ManagedFields.Values.containsKey(paramFormField1.key)) {
      paramFormField2.control = paramFormField1.control;
      paramFormField2.type = paramFormField1.type;
      if (paramFormField2.key.equals("user.password"))
        paramFormField2.required = true; 
    } 
    paramFormField2.normalize();
    this.formMapper.updateField(paramFormField2);
    setSyntheticConsentFields(paramFormField2);
  }
  
  public FormService.ValidationResult validateCreate(Form paramForm) {
    FormService.ValidationResult validationResult = new FormService.ValidationResult();
    validationResult




      
      .errors = (new Validator()).notDuplicate(this.formMapper.retrieveExisting(null, paramForm.name), "form.name", new Object[] { paramForm.name }).withErrors(commonFormValidation(paramForm)).done();
    validationResult.form = paramForm;
    return validationResult;
  }
  
  public FormService.ValidationResult validateDelete(UUID paramUUID) {
    FormService.ValidationResult validationResult = new FormService.ValidationResult();
    validationResult.existing = retrieveById(paramUUID);
    validationResult











      
      .errors = (new Validator()).emptyWithCode(this.applicationMapper.retrieveApplicationIdsUsingFormById(paramUUID), "formId", "[inUse]formId", paramCollection -> (String)paramCollection.stream().map(UUID::toString).collect(Collectors.joining(", "))).emptyWithCode(this.tenantMapper.retrieveTenantIdsUsingFormById(paramUUID), "formId", "[inUseByTenant]formId", paramCollection -> (String)paramCollection.stream().map(UUID::toString).collect(Collectors.joining(", "))).notInUseWithCode((this.tenantManagerMapper.retrieveTenantManagerConfigurationCountUsingFormById(paramUUID) == 0), "formId", "[inUseByTenantManagerConfiguration]formId", new Object[0]).done();
    return validationResult;
  }
  
  public FormService.FieldValidationResult validateFieldCreate(FormField paramFormField) {
    FormService.FieldValidationResult fieldValidationResult = new FormService.FieldValidationResult();
    fieldValidationResult











      
      .errors = (new Validator()).notDuplicate(this.formMapper.retrieveExistingField(null, paramFormField.name), "field.name", new Object[] { paramFormField.name }).ifTrue((paramFormField.type != FormDataType.consent && paramFormField.consentId == null), paramValidator -> paramValidator.notBlank(paramFormField.key, "field.key", new Object[0]).ifLastCheckHadNoError(())).withErrors(commonFieldValidation(paramFormField)).done();
    fieldValidationResult.field = paramFormField;
    return fieldValidationResult;
  }
  
  public FormService.FieldValidationResult validateFieldDelete(UUID paramUUID) {
    FormService.FieldValidationResult fieldValidationResult = new FormService.FieldValidationResult();
    fieldValidationResult.existing = retrieveFieldById(paramUUID);
    fieldValidationResult




      
      .errors = (new Validator()).forEach(retrieveAll(), (paramValidator, paramForm, paramInteger) -> paramValidator.notInUse(!usesField(paramForm, paramUUID), "fieldId", new Object[] { paramUUID, paramForm.name, paramForm.id })).done();
    return fieldValidationResult;
  }
  
  public FormService.FieldValidationResult validateFieldUpdate(FormField paramFormField) {
    FormService.FieldValidationResult fieldValidationResult = new FormService.FieldValidationResult();
    fieldValidationResult.existing = (paramFormField.id != null) ? retrieveFieldById(paramFormField.id) : null;
    if (fieldValidationResult.existing == null)
      return fieldValidationResult; 
    fieldValidationResult










      
      .errors = (new Validator()).ifLastCheckHadNoError(paramValidator -> paramValidator.notDuplicate(this.formMapper.retrieveExistingField(paramFormField.id, paramFormField.name), "field.name", new Object[] { paramFormField.name })).ifTrue((fieldValidationResult.existing.type != FormDataType.consent), paramValidator -> paramValidator.ifFalse(ManagedFields.Values.containsKey(paramFieldValidationResult.existing.key), ())).withErrors(commonFieldValidation(paramFormField)).done();
    fieldValidationResult.field = paramFormField;
    return fieldValidationResult;
  }
  
  public FormService.ValidationResult validateUpdate(Form paramForm) {
    FormService.ValidationResult validationResult = new FormService.ValidationResult();
    validationResult.existing = (paramForm.id != null) ? retrieveById(paramForm.id) : null;
    if (validationResult.existing == null)
      return validationResult; 
    paramForm.type = validationResult.existing.type;
    validationResult






      
      .errors = (new Validator()).notDuplicate(this.formMapper.retrieveExisting(paramForm.id, paramForm.name), "form.name", new Object[] { paramForm.name }).ifTrue(isFormInUse(paramForm), paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(this.reactorStatusService.retrieveStatus(), ()), "form.id", "[notLicensed]", new Object[0])).withErrors(commonFormValidation(paramForm)).done();
    validationResult.form = paramForm;
    return validationResult;
  }
  
  private Errors commonFieldValidation(FormField paramFormField) {
    return (new Validator())
      
      .notBlank(paramFormField.name, "field.name", new Object[0])

      
      .ifTrue((paramFormField.type == FormDataType.consent), paramValidator -> paramValidator.notMissing(paramFormField.consentId, "field.consentId", new Object[0]))
      .ifTrue((paramFormField.consentId != null), paramValidator -> paramValidator.ensure((paramFormField.type == FormDataType.consent), "field.type", "[invalid]", new Object[0]).ifLastCheckHadNoError(()))






      
      .ifTrue(paramFormField.validator.enabled, paramValidator -> paramValidator.notBlank(paramFormField.validator.expression, "field.validator.expression", new Object[0]).ifLastCheckHadNoError(()))


      
      .ifTrue(validateOptions(paramFormField), paramValidator -> paramValidator.notEmpty(paramFormField.options, "field.options", new Object[] { paramFormField.control }).ifLastCheckHadNoError(())).done();
  }
  
  private Errors commonFormValidation(Form paramForm) {
    Map map = (Map)this.formMapper.retrieveAllFields().stream().collect(Collectors.toMap(paramFormField -> paramFormField.id, paramFormField -> paramFormField));
    return (new Validator())
      
      .notBlank(paramForm.name, "form.name", new Object[0])
      .notEmpty(paramForm.steps, "form.steps", new Object[0])

      
      .ifLastCheckHadNoError(paramValidator -> paramValidator.withErrors(validateByFormType(paramForm, paramMap)))


      
      .forEach(paramForm.steps, (paramValidator, paramFormStep, paramInteger) -> paramValidator.forEach(paramFormStep.fields, ()))






      
      .ifNoErrors(paramValidator -> paramValidator.withErrors(validateNoUniqueFields(paramForm, paramMap)))
      
      .done();
  }
  
  private void createFormSteps(Form paramForm) {
    for (byte b = 0; b < paramForm.steps.size(); b++) {
      FormStep formStep = paramForm.steps.get(b);
      if (formStep.type == FormStepType.collectData) {
        this.formMapper.createCollectDataFormStep(paramForm.id, b, FormStepType.collectData, formStep.fields);
      } else {
        this.formMapper.createNonDataFormStep(paramForm.id, b, formStep.type);
      } 
    } 
  }
  
  private boolean isFormInUse(Form paramForm) {
    switch (paramForm.type) {
      default:
        throw new MatchException(null, null);
      case bool:
      
      case date:
      
      case number:
      
      case string:
        break;
    } 
    return (


      
      this.formMapper.retrieveSelfServiceUserInUseCount(paramForm.id) > 0);
  }
  
  private void setSyntheticConsentFields(FormField paramFormField) {
    if (paramFormField == null)
      return; 
    if (paramFormField.type == FormDataType.consent) {
      Consent consent = this.consentMapper.retrieveConsentById(paramFormField.consentId);
      paramFormField.control = (consent.values.isEmpty() || consent.multipleValuesAllowed) ? FormControl.checkbox : FormControl.radio;
      paramFormField.options = consent.values;
    } 
  }
  
  private boolean usesField(Form paramForm, UUID paramUUID) {
    return paramForm.steps.stream()
      .flatMap(paramFormStep -> paramFormStep.fields.stream())
      .anyMatch(paramUUID2 -> paramUUID2.equals(paramUUID1));
  }
  
  private boolean validKey(String paramString) {
    if (ManagedFields.Values.containsKey(paramString))
      return true; 
    if (paramString.startsWith("user.data.") && paramString.length() > "user.data.".length())
      return FormTools.validateFieldKey(paramString.substring("user.data".length())); 
    if (paramString.startsWith("registration.data.") && paramString.length() > "registration.data.".length())
      return FormTools.validateFieldKey(paramString.substring("registration.data".length())); 
    return false;
  }
  
  private String validKeyList() {
    return String.join(", ", ManagedFields.Values.keySet().stream()
        .sorted().toList());
  }
  
  private Errors validKeysForFormType(Form paramForm, Map<UUID, FormField> paramMap) {
    switch (paramForm.type) {
      case bool:
      
      case date:
      
      case string:
      
    } 
    String str = 




      
      "";
    Validator validator = (new Validator()).forEach(paramForm.steps, (paramValidator, paramFormStep, paramInteger) -> paramValidator.forEach(paramFormStep.fields, ()));
    return validator.done();
  }
  
  private Errors validateByFormType(Form paramForm, Map<UUID, FormField> paramMap) {
    Validator validator = new Validator();
    if (paramForm.type == FormType.registration || paramForm.type == null) {
      validateRegistrationForm(validator, paramForm, paramMap);
    } else {
      validator.forEach(paramForm.steps, (paramValidator, paramFormStep, paramInteger) -> paramValidator.validWithCode((paramFormStep.type == FormStepType.collectData), "form.steps[%d].type".formatted(new Object[] { paramInteger }, ), "[invalidFormType]form.steps.type", new Object[] { paramFormStep.type })).ifLastCheckHadNoError(paramValidator -> paramValidator.withErrors(validKeysForFormType(paramForm, paramMap)));
    } 
    return validator.done();
  }
  
  private boolean validateFieldOptions(List<String> paramList, FormDataType paramFormDataType) {
    switch (paramFormDataType) {
      default:
        throw new MatchException(null, null);
      case bool:
      
      case date:
      
      case number:
      
      case string:
      
      case consent:
      case email:
      case phoneNumber:
        break;
    } 
    return false;
  }
  
  private Errors validateNoUniqueFields(Form paramForm, Map<UUID, FormField> paramMap) {
    Errors errors = new Errors();
    HashSet<String> hashSet = new HashSet();
    for (byte b = 0; b < paramForm.steps.size(); b++) {
      FormStep formStep = paramForm.steps.get(b);
      for (byte b1 = 0; b1 < formStep.fields.size(); b1++) {
        if (!hashSet.add(((FormField)paramMap.get(formStep.fields.get(b1))).key))
          errors.addFieldError("form.steps[" + b + "].fields[" + b1 + "]", "[duplicate]form.steps.fields", null, new Object[0]); 
      } 
    } 
    return errors;
  }
  
  private boolean validateOptions(FormField paramFormField) {
    return (RequireOptions.contains(paramFormField.control) && !ManagedFields.Values.containsKey(paramFormField.key));
  }
  
  private void validateRegistrationForm(Validator paramValidator, Form paramForm, Map<UUID, FormField> paramMap) {
    HashMap<Object, Object> hashMap1 = new HashMap<>();
    HashMap<Object, Object> hashMap2 = new HashMap<>();
    HashMap<Object, Object> hashMap3 = new HashMap<>();
    Integer integer1 = null;
    Integer integer2 = null;
    for (byte b = 0; b < paramForm.steps.size(); b++) {
      FormStep formStep = paramForm.steps.get(b);
      if (formStep.type == FormStepType.verifyEmail || formStep.type == FormStepType.verifyPhoneNumber) {
        integer2 = Integer.valueOf(b);
        hashMap3.putIfAbsent(formStep.type, Integer.valueOf(b));
      } 
      for (UUID uUID : formStep.fields) {
        FormField formField = paramMap.get(uUID);
        if (formField == null)
          continue; 
        if (integer1 == null && formField.key.equals("user.password"))
          integer1 = Integer.valueOf(b); 
        hashMap1.put(formField.key, formField);
        hashMap2.put(formField.key, Integer.valueOf(b));
      } 
    } 
    Integer integer3 = integer1;
    Integer integer4 = integer2;
    paramValidator.ifTrue((integer1 != null && integer2 != null), paramValidator -> paramValidator.ensureWithCode((paramInteger1.intValue() > paramInteger2.intValue()), "form.steps[%d].fields".formatted(new Object[] { paramInteger1 }, ), "[invalidPassword]form.steps.fields", new Object[0])).ensure(hashMap1.values()
        .stream()
        .anyMatch(paramFormField -> {
            String str = paramFormField.key;
            return ((str.equals("user.email") || str.equals("user.phoneNumber") || str.equals("user.username")) && paramFormField.required);
          }), "form.steps", "[invalid]", new Object[] { paramForm.type }).forEach(paramForm.steps, (paramFormStep, paramInteger) -> {
          BiFunction biFunction1 = ();
          BiFunction biFunction2 = ();
          paramValidator.ifFalse((paramFormStep.type == FormStepType.collectData), ()).ifTrue((paramFormStep.type == FormStepType.verifyEmail), ()).ifTrue((paramFormStep.type == FormStepType.verifyPhoneNumber), ());
        });
  }
  
  private boolean validateTypeAndCheckForDuplicates(List<String> paramList, Function<String, Object> paramFunction) {
    return 


      
      (paramList.stream().<Object>map(paramFunction).filter(Objects::nonNull).distinct().count() == paramList.size());
  }
}
