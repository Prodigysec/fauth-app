package io.fusionauth.api.domain;

import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStepType;
import io.fusionauth.domain.form.FormType;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;

public interface FormMapper {
  void create(Form paramForm);
  
  void createCollectDataFormStep(@Param("formId") UUID paramUUID, @Param("step") int paramInt, @Param("type") FormStepType paramFormStepType, @Param("fieldIds") List<UUID> paramList);
  
  void createField(FormField paramFormField);
  
  void createNonDataFormStep(@Param("formId") UUID paramUUID, @Param("step") int paramInt, @Param("type") FormStepType paramFormStepType);
  
  void delete(UUID paramUUID);
  
  void deleteField(UUID paramUUID);
  
  void deleteFormSteps(@Param("formId") UUID paramUUID);
  
  int retrieveAdminRegistrationInUseCount(@Param("formId") UUID paramUUID);
  
  int retrieveAdminUserInUseByCount(@Param("formId") UUID paramUUID);
  
  List<Form> retrieveAll();
  
  List<Form> retrieveAllByType(FormType paramFormType);
  
  List<FormField> retrieveAllFields();
  
  Form retrieveById(UUID paramUUID);
  
  Form retrieveExisting(@Param("id") UUID paramUUID, @Param("name") String paramString);
  
  FormField retrieveExistingField(@Param("id") UUID paramUUID, @Param("name") String paramString);
  
  FormField retrieveFieldById(@Param("id") UUID paramUUID);
  
  List<UUID> retrieveFieldIdByConsentId(UUID paramUUID);
  
  int retrieveRegistrationInUseCount(@Param("formId") UUID paramUUID);
  
  int retrieveSelfServiceUserInUseCount(@Param("formId") UUID paramUUID);
  
  void update(Form paramForm);
  
  void updateField(FormField paramFormField);
}
