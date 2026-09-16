package io.fusionauth.api.service.lambda;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.CompiledLambda;
import io.fusionauth.api.domain.ConnectorConfigurationMapper;
import io.fusionauth.api.domain.IdentityProviderMapper;
import io.fusionauth.api.domain.LambdaMapper;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.util.InUseValidator;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.LambdaEngineType;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorType;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.search.LambdaSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class DefaultLambdaService implements LambdaService {
  private final ApplicationMapper applicationMapper;
  
  private final CacheNotifier cacheNotifier;
  
  private final ConnectorConfigurationMapper connectorConfigurationMapper;
  
  private final IdentityProviderMapper identityProviderMapper;
  
  private final InUseValidator inUseValidator;
  
  private final LambdaEngine lambdaEngine;
  
  private final LambdaMapper lambdaMapper;
  
  private final TenantMapper tenantMapper;
  
  @Inject
  public DefaultLambdaService(ApplicationMapper paramApplicationMapper, CacheNotifier paramCacheNotifier, ConnectorConfigurationMapper paramConnectorConfigurationMapper, IdentityProviderMapper paramIdentityProviderMapper, InUseValidator paramInUseValidator, LambdaEngine paramLambdaEngine, LambdaMapper paramLambdaMapper, TenantMapper paramTenantMapper) {
    this.applicationMapper = paramApplicationMapper;
    this.cacheNotifier = paramCacheNotifier;
    this.connectorConfigurationMapper = paramConnectorConfigurationMapper;
    this.identityProviderMapper = paramIdentityProviderMapper;
    this.inUseValidator = paramInUseValidator;
    this.lambdaEngine = paramLambdaEngine;
    this.lambdaMapper = paramLambdaMapper;
    this.tenantMapper = paramTenantMapper;
  }
  
  public void create(Lambda paramLambda) {
    if (paramLambda.id == null)
      paramLambda.id = UUID.randomUUID(); 
    paramLambda.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramLambda.lastUpdateInstant = paramLambda.insertInstant;
    this.lambdaMapper.create(paramLambda);
    this.cacheNotifier.reload("Lambdas");
  }
  
  public void delete(UUID paramUUID) {
    if (this.lambdaMapper.delete(paramUUID) == 0)
      throw new NotFoundException(); 
    this.cacheNotifier.reload("Lambdas");
  }
  
  public List<Lambda> retrieveAll() {
    return this.lambdaMapper.retrieveAll();
  }
  
  public Lambda retrieveById(UUID paramUUID) {
    return this.lambdaMapper.retrieveById(paramUUID);
  }
  
  public List<Lambda> retrieveEnabledByType(LambdaType paramLambdaType) {
    return this.lambdaMapper.retrieveEnabledByType(paramLambdaType);
  }
  
  public SearchResults<Lambda> search(LambdaSearchCriteria paramLambdaSearchCriteria) {
    int i = this.lambdaMapper.retrieveCountByCriteria(paramLambdaSearchCriteria);
    List<Lambda> list = (i > 0) ? this.lambdaMapper.retrieveByCriteria(paramLambdaSearchCriteria) : List.of();
    return new SearchResults<>(list, i);
  }
  
  public void update(Lambda paramLambda1, Lambda paramLambda2) {
    paramLambda2.type = paramLambda1.type;
    paramLambda2.insertInstant = paramLambda1.insertInstant;
    paramLambda2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.lambdaMapper.update(paramLambda2);
    this.cacheNotifier.reload("Lambdas");
  }
  
  public LambdaService.ValidationResult validate(Lambda paramLambda, boolean paramBoolean) {
    LambdaService.ValidationResult validationResult = new LambdaService.ValidationResult();
    validationResult.existing = retrieveById(paramLambda.id);
    validationResult






      
      .errors = (new Validator()).ifTrue(paramBoolean, paramValidator -> paramValidator.notDuplicate((paramLambda.id != null) ? paramValidationResult.existing : null, "lambdaId", new Object[] { paramLambda.id }).notMissing(paramLambda.type, "lambda.type", new Object[0])).ifTrue(!paramBoolean, paramValidator -> paramValidator.notMissing(paramLambda.id, "lambdaId", new Object[0])).valid((LambdaEngineType.GraalJS == paramLambda.engineType), "lambda.engineType", new Object[] { paramLambda.engineType.name(), LambdaEngineType.GraalJS.name() }).notBlank(paramLambda.body, "lambda.body", new Object[0]).notBlank(paramLambda.name, "lambda.name", new Object[0]).ifTrue((paramLambda.body != null), paramValidator -> checkCode(paramValidator, paramLambda, paramValidationResult.existing)).done();
    return validationResult;
  }
  
  public Errors validateDelete(UUID paramUUID) {
    return (new Validator())

      
      .forEach(this.applicationMapper.retrieveAllIgnoreActive(null), (paramValidator, paramApplication, paramInteger) -> paramValidator.validate(()).validate(()).validate(()).validate(()).validate(()).validate(()))








      
      .forEach(this.tenantMapper.retrieveAll(), (paramValidator, paramTenant, paramInteger) -> paramValidator.validate(()).validate(()).validate(()).validate(()).validate(()).validate(()).validate(()).validate(()).validate(()))










      
      .forEach(this.connectorConfigurationMapper.retrieveAllByType(ConnectorType.LDAP), (paramValidator, paramBaseConnectorConfiguration, paramInteger) -> paramValidator.validate(()))


      
      .forEach(this.identityProviderMapper.retrieveAll(null), (paramValidator, paramBaseIdentityProvider, paramInteger) -> paramValidator.validate(()))

      
      .done();
  }
  
  private void checkCode(Validator paramValidator, Lambda paramLambda1, Lambda paramLambda2) {
    try {
      paramLambda1.type = (paramLambda2 != null) ? paramLambda2.type : paramLambda1.type;
      CompiledLambda compiledLambda = new CompiledLambda(paramLambda1);
      if (compiledLambda.type != null) {
        CompiledLambda.InvocationResult invocationResult = this.lambdaEngine.invoke(compiledLambda, new io.fusionauth.api.domain.api.service.LambdaArgument[0]);
        if (invocationResult.exception instanceof NoSuchMethodException)
          paramValidator.validWithCode(false, "lambda.body", "[functionMissing]lambda.body", new Object[] { compiledLambda.type.getFunctionName() }); 
      } 
    } catch (Exception exception) {
      String str = ((exception.getCause() != null) ? exception.getCause().getMessage() : exception.getMessage()).replace("\n", "\\n");
      paramValidator.valid(false, "lambda.body", new Object[] { str });
    } 
  }
}
