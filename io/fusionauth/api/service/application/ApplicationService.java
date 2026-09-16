package io.fusionauth.api.service.application;

import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationOAuthScope;
import io.fusionauth.domain.ApplicationPhoneConfiguration;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface ApplicationService {
  public static final List<Field> PhoneTemplateIdFields;
  
  void create(Tenant paramTenant, Application paramApplication);
  
  void createOAuthScope(Tenant paramTenant, ApplicationOAuthScope paramApplicationOAuthScope);
  
  void createRole(Tenant paramTenant, ApplicationRole paramApplicationRole);
  
  boolean deactivate(Tenant paramTenant, Application paramApplication);
  
  boolean delete(Tenant paramTenant, Application paramApplication);
  
  void deleteAllByTenantId(UUID paramUUID);
  
  void deleteOAuthScope(ApplicationOAuthScope paramApplicationOAuthScope);
  
  void deleteRole(ApplicationRole paramApplicationRole);
  
  void reactivate(Application paramApplication);
  
  void update(Tenant paramTenant, Application paramApplication1, Application paramApplication2, EventInfo paramEventInfo);
  
  ApplicationOAuthScope updateOAuthScope(Tenant paramTenant, ApplicationOAuthScope paramApplicationOAuthScope);
  
  ApplicationRole updateRole(Tenant paramTenant, ApplicationRole paramApplicationRole);
  
  ValidationResult validateApplicationId(Tenant paramTenant, UUID paramUUID);
  
  ValidationResult validateCopy(Tenant paramTenant, Application paramApplication, UUID paramUUID);
  
  ValidationResult validateCreate(Tenant paramTenant, Application paramApplication);
  
  ValidationResult validateOAuthScopeCreate(Tenant paramTenant, UUID paramUUID, ApplicationOAuthScope paramApplicationOAuthScope);
  
  ValidationResult validateOAuthScopeRetrieveById(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2);
  
  ValidationResult validateOAuthScopeUpdate(Tenant paramTenant, UUID paramUUID, ApplicationOAuthScope paramApplicationOAuthScope);
  
  ValidationResult validateReactivate(Tenant paramTenant, UUID paramUUID);
  
  ValidationResult validateRoleCreate(Tenant paramTenant, UUID paramUUID, ApplicationRole paramApplicationRole);
  
  ValidationResult validateRoleDelete(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString);
  
  ValidationResult validateRoleUpdate(Tenant paramTenant, UUID paramUUID, ApplicationRole paramApplicationRole);
  
  ValidationResult validateUpdate(Tenant paramTenant, Application paramApplication, UUID paramUUID);
  
  static {
    PhoneTemplateIdFields = (List<Field>)Arrays.<Field>stream(ApplicationPhoneConfiguration.class.getDeclaredFields()).filter(paramField -> paramField.getName().endsWith("TemplateId")).sorted(Comparator.comparing(Field::getName)).collect(Collectors.toList());
  }
  
  public static final List<String> PhoneTemplateIdFieldNames = (List<String>)PhoneTemplateIdFields.stream().map(Field::getName).collect(Collectors.toList());
  
  public static class ValidationResult extends BaseValidationResult {
    public Application application;
    
    public Application existing;
    
    public ApplicationRole role;
    
    public ApplicationOAuthScope scope;
    
    public Tenant tenant;
  }
}
