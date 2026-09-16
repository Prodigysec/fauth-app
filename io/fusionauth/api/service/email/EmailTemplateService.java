package io.fusionauth.api.service.email;

import com.inversoft.error.Errors;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.email.EmailTemplate;
import io.fusionauth.domain.search.EmailTemplateSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;

public interface EmailTemplateService {
  void create(EmailTemplate paramEmailTemplate);
  
  boolean delete(UUID paramUUID);
  
  List<EmailTemplate> retrieveAll();
  
  EmailTemplate retrieveById(UUID paramUUID);
  
  EmailTemplate retrieveByName(String paramString);
  
  SearchResults<EmailTemplate> search(EmailTemplateSearchCriteria paramEmailTemplateSearchCriteria);
  
  boolean update(EmailTemplate paramEmailTemplate);
  
  Errors validate(EmailTemplate paramEmailTemplate, boolean paramBoolean);
  
  ValidationResult validateDelete(UUID paramUUID);
  
  public static class ValidationResult extends BaseValidationResult {
    public EmailTemplate existing;
  }
}
