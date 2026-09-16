package io.fusionauth.api.service.family;

import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.Family;
import io.fusionauth.domain.FamilyMember;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import java.util.List;
import java.util.UUID;

public interface FamilyService {
  boolean removeMember(UUID paramUUID1, UUID paramUUID2, UUID paramUUID3);
  
  Family retrieveById(UUID paramUUID1, UUID paramUUID2);
  
  List<Family> retrieveByUserId(UUID paramUUID1, UUID paramUUID2);
  
  void sendFamilyRequestEmail(Tenant paramTenant, String paramString);
  
  Family upsertMember(UUID paramUUID, Family paramFamily, User paramUser, FamilyMember paramFamilyMember);
  
  ValidationResult validateCreate(Tenant paramTenant, UUID paramUUID, FamilyMember paramFamilyMember);
  
  ValidationResult validateUpdate(Tenant paramTenant, UUID paramUUID, FamilyMember paramFamilyMember);
  
  public static class ValidationResult extends BaseValidationResult {
    public Family family;
    
    public Tenant tenant;
    
    public User user;
  }
}
