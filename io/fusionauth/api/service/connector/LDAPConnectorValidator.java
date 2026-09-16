package io.fusionauth.api.service.connector;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.lambda.LambdaService;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.LDAPConnectorConfiguration;

public class LDAPConnectorValidator implements ConnectorValidator {
  private final LambdaService lambdaService;
  
  @Inject
  public LDAPConnectorValidator(LambdaService paramLambdaService) {
    this.lambdaService = paramLambdaService;
  }
  
  public Errors validate(BaseConnectorConfiguration paramBaseConnectorConfiguration, boolean paramBoolean) {
    LDAPConnectorConfiguration lDAPConnectorConfiguration = (LDAPConnectorConfiguration)paramBaseConnectorConfiguration;
    Lambda lambda = (lDAPConnectorConfiguration.lambdaConfiguration.reconcileId == null) ? null : this.lambdaService.retrieveById(lDAPConnectorConfiguration.lambdaConfiguration.reconcileId);
    return (new Validator())
      .notBlank(lDAPConnectorConfiguration.authenticationURL, "connector.authenticationURL", new Object[0])
      
      .valid((lDAPConnectorConfiguration.connectTimeout > 0), "connector.connectTimeout", new Object[0])
      .valid((lDAPConnectorConfiguration.readTimeout > 0), "connector.readTimeout", new Object[0])

      
      .notMissing(lDAPConnectorConfiguration.lambdaConfiguration.reconcileId, "connector.lambdaConfiguration.reconcileId", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.valid((paramLambda != null), "connector.lambdaConfiguration.reconcileId", new Object[] { paramLDAPConnectorConfiguration.lambdaConfiguration.reconcileId }).ifLastCheckHadNoError(())).notBlank(lDAPConnectorConfiguration.baseStructure, "connector.baseStructure", new Object[0])
      .notBlank(lDAPConnectorConfiguration.loginIdAttribute, "connector.loginIdAttribute", new Object[0])
      .notBlank(lDAPConnectorConfiguration.identifyingAttribute, "connector.identifyingAttribute", new Object[0])
      .notEmpty(lDAPConnectorConfiguration.requestedAttributes, "connector.requestedAttributes", new Object[0])
      .notMissing(lDAPConnectorConfiguration.securityMethod, "connector.securityMethod", new Object[0])
      .notBlank(lDAPConnectorConfiguration.systemAccountDN, "connector.systemAccountDN", new Object[0])
      .notBlank(lDAPConnectorConfiguration.systemAccountPassword, "connector.systemAccountPassword", new Object[0])
      .done();
  }
}
