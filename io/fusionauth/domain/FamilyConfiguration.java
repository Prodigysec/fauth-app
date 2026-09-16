package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import java.util.Objects;
import java.util.UUID;

public class FamilyConfiguration extends Enableable implements Buildable<FamilyConfiguration> {
  public boolean allowChildRegistrations = true;
  
  @ExcludeFromJSONColumn
  public UUID confirmChildEmailTemplateId;
  
  public boolean deleteOrphanedAccounts;
  
  public int deleteOrphanedAccountsDays = 30;
  
  @ExcludeFromJSONColumn
  public UUID familyRequestEmailTemplateId;
  
  public int maximumChildAge = 12;
  
  public int minimumOwnerAge = 21;
  
  public boolean parentEmailRequired;
  
  @ExcludeFromJSONColumn
  public UUID parentRegistrationEmailTemplateId;
  
  @JacksonConstructor
  public FamilyConfiguration() {}
  
  public FamilyConfiguration(FamilyConfiguration paramFamilyConfiguration) {
    this.allowChildRegistrations = paramFamilyConfiguration.allowChildRegistrations;
    this.confirmChildEmailTemplateId = paramFamilyConfiguration.confirmChildEmailTemplateId;
    this.deleteOrphanedAccounts = paramFamilyConfiguration.deleteOrphanedAccounts;
    this.deleteOrphanedAccountsDays = paramFamilyConfiguration.deleteOrphanedAccountsDays;
    this.enabled = paramFamilyConfiguration.enabled;
    this.familyRequestEmailTemplateId = paramFamilyConfiguration.familyRequestEmailTemplateId;
    this.maximumChildAge = paramFamilyConfiguration.maximumChildAge;
    this.minimumOwnerAge = paramFamilyConfiguration.minimumOwnerAge;
    this.parentEmailRequired = paramFamilyConfiguration.parentEmailRequired;
    this.parentRegistrationEmailTemplateId = paramFamilyConfiguration.parentRegistrationEmailTemplateId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof FamilyConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    FamilyConfiguration familyConfiguration = (FamilyConfiguration)paramObject;
    return (this.allowChildRegistrations == familyConfiguration.allowChildRegistrations && this.deleteOrphanedAccounts == familyConfiguration.deleteOrphanedAccounts && this.deleteOrphanedAccountsDays == familyConfiguration.deleteOrphanedAccountsDays && this.maximumChildAge == familyConfiguration.maximumChildAge && this.minimumOwnerAge == familyConfiguration.minimumOwnerAge && this.parentEmailRequired == familyConfiguration.parentEmailRequired && 




      
      Objects.equals(this.familyRequestEmailTemplateId, familyConfiguration.familyRequestEmailTemplateId) && 
      Objects.equals(this.confirmChildEmailTemplateId, familyConfiguration.confirmChildEmailTemplateId) && 
      Objects.equals(this.parentRegistrationEmailTemplateId, familyConfiguration.parentRegistrationEmailTemplateId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), Boolean.valueOf(this.allowChildRegistrations), this.familyRequestEmailTemplateId, this.confirmChildEmailTemplateId, 
          Boolean.valueOf(this.deleteOrphanedAccounts), Integer.valueOf(this.deleteOrphanedAccountsDays), Integer.valueOf(this.maximumChildAge), Integer.valueOf(this.minimumOwnerAge), Boolean.valueOf(this.parentEmailRequired), this.parentRegistrationEmailTemplateId });
  }
  
  public void normalize() {
    if (!this.allowChildRegistrations)
      this.parentEmailRequired = false; 
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
