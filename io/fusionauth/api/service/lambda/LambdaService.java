package io.fusionauth.api.service.lambda;

import com.inversoft.error.Errors;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.search.LambdaSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;

public interface LambdaService {
  void create(Lambda paramLambda);
  
  void delete(UUID paramUUID);
  
  List<Lambda> retrieveAll();
  
  Lambda retrieveById(UUID paramUUID);
  
  List<Lambda> retrieveEnabledByType(LambdaType paramLambdaType);
  
  SearchResults<Lambda> search(LambdaSearchCriteria paramLambdaSearchCriteria);
  
  void update(Lambda paramLambda1, Lambda paramLambda2);
  
  ValidationResult validate(Lambda paramLambda, boolean paramBoolean);
  
  Errors validateDelete(UUID paramUUID);
  
  public static class ValidationResult extends BaseValidationResult {
    public Lambda existing;
    
    public Lambda lambda;
  }
}
