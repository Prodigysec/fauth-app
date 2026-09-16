package io.fusionauth.api.service.consent;

import com.inversoft.error.Errors;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.UserConsent;
import io.fusionauth.domain.search.ConsentSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;

public interface ConsentService {
  void createConsent(Consent paramConsent);
  
  void createUserConsent(UserConsent paramUserConsent, boolean paramBoolean);
  
  void createUserConsentWithStatus(UserConsent paramUserConsent, boolean paramBoolean);
  
  boolean deleteConsent(Consent paramConsent);
  
  void handleEmailPlusFollowup();
  
  List<Consent> retrieveAllConsents();
  
  Consent retrieveConsentById(UUID paramUUID);
  
  UserConsent retrieveUserConsentById(UUID paramUUID1, UUID paramUUID2);
  
  List<UserConsent> retrieveUserConsentByUserId(UUID paramUUID1, UUID paramUUID2);
  
  void revokeUserConsent(UUID paramUUID1, UUID paramUUID2);
  
  SearchResults<Consent> search(ConsentSearchCriteria paramConsentSearchCriteria);
  
  void updateConsent(Consent paramConsent1, Consent paramConsent2);
  
  UserConsent updateUserConsent(UUID paramUUID, UserConsent paramUserConsent);
  
  ValidationResult validateConsent(Consent paramConsent, boolean paramBoolean);
  
  ValidationResult validateDelete(UUID paramUUID);
  
  Errors validateUserConsent(UUID paramUUID, UserConsent paramUserConsent, boolean paramBoolean);
  
  public static class ValidationResult extends BaseValidationResult {
    public Consent consent;
    
    public Consent existing;
  }
}
