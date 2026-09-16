package io.fusionauth.api.service.system;

import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.EmailConfiguration;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TenantPhoneConfiguration;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface TenantService {
  public static final List<String> EmailTemplateIdFieldNames;
  
  public static final List<Field> EmailTemplateIdFields;
  
  public static final List<Field> PhoneTemplateIdFields;
  
  static {
    EmailTemplateIdFieldNames = (List<String>)Arrays.<Field>stream(EmailConfiguration.class.getDeclaredFields()).map(Field::getName).filter(paramString -> paramString.endsWith("EmailTemplateId")).sorted().collect(Collectors.toList());
    EmailTemplateIdFields = (List<Field>)Arrays.<Field>stream(EmailConfiguration.class.getDeclaredFields()).filter(paramField -> paramField.getName().endsWith("EmailTemplateId")).sorted(Comparator.comparing(Field::getName)).collect(Collectors.toList());
    PhoneTemplateIdFields = (List<Field>)Arrays.<Field>stream(TenantPhoneConfiguration.class.getDeclaredFields()).filter(paramField -> paramField.getName().endsWith("TemplateId")).sorted(Comparator.comparing(Field::getName)).collect(Collectors.toList());
  }
  
  public static final List<String> PhoneTemplateIdFieldNames = (List<String>)PhoneTemplateIdFields.stream().map(Field::getName).collect(Collectors.toList());
  
  static UUID optionalTenantId(Tenant paramTenant) {
    return (paramTenant == null) ? null : paramTenant.id;
  }
  
  void create(Tenant paramTenant, List<UUID> paramList);
  
  void delete(Tenant paramTenant, EventInfo paramEventInfo);
  
  void deleteAsync(Tenant paramTenant, EventInfo paramEventInfo);
  
  void setConfigured(Tenant paramTenant);
  
  void update(Tenant paramTenant1, Tenant paramTenant2, EventInfo paramEventInfo, List<UUID> paramList);
  
  ValidationResult validate(Tenant paramTenant, boolean paramBoolean, List<UUID> paramList, UUID paramUUID);
  
  ValidationResult validateCopy(Tenant paramTenant, UUID paramUUID);
  
  ValidationResult validateDelete(Tenant paramTenant, UUID paramUUID);
  
  ValidationResult validateGet(Tenant paramTenant, UUID paramUUID);
  
  public static class ValidationResult extends BaseValidationResult {
    public Tenant existing;
    
    public Tenant tenant;
  }
}
