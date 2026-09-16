package io.fusionauth.api.util;

import com.google.inject.Inject;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.LambdaMapper;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.LambdaType;
import java.util.UUID;

public class LambdaValidator {
  private final LambdaMapper lambdaMapper;
  
  @Inject
  public LambdaValidator(LambdaMapper paramLambdaMapper) {
    this.lambdaMapper = paramLambdaMapper;
  }
  
  public void validate(Validator paramValidator, boolean paramBoolean, UUID paramUUID, LambdaType paramLambdaType, String paramString) {
    if (paramBoolean) {
      validateRequired(paramValidator, paramUUID, paramLambdaType, paramString);
    } else {
      validateOptional(paramValidator, paramUUID, paramLambdaType, paramString);
    } 
  }
  
  public void validateOptional(Validator paramValidator, UUID paramUUID, LambdaType paramLambdaType, String paramString) {
    paramValidator.ifTrue((paramUUID != null), () -> paramValidator.holdMyBeer(this.lambdaMapper.retrieveById(paramUUID)).validObject(paramValidator.barkeep(), paramString, new Object[] { paramUUID }).ifLastCheckHadNoError(()));
  }
  
  public void validateRequired(Validator paramValidator, UUID paramUUID, LambdaType paramLambdaType, String paramString) {
    paramValidator.notMissing(paramUUID, paramString, new Object[] { paramUUID }).ifLastCheckHadNoError(() -> validateOptional(paramValidator, paramUUID, paramLambdaType, paramString));
  }
}
