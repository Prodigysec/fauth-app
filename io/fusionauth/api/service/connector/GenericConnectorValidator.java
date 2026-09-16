package io.fusionauth.api.service.connector;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.GenericConnectorConfiguration;

public class GenericConnectorValidator implements ConnectorValidator {
  private final KeyReaderService keyReader;
  
  @Inject
  public GenericConnectorValidator(KeyReaderService paramKeyReaderService) {
    this.keyReader = paramKeyReaderService;
  }
  
  public Errors validate(BaseConnectorConfiguration paramBaseConnectorConfiguration, boolean paramBoolean) {
    GenericConnectorConfiguration genericConnectorConfiguration = (GenericConnectorConfiguration)paramBaseConnectorConfiguration;
    Key key = (genericConnectorConfiguration.sslCertificateKeyId == null) ? null : this.keyReader.retrieveById(genericConnectorConfiguration.sslCertificateKeyId);
    return (new Validator())
      .notBlank(genericConnectorConfiguration.authenticationURL, "connector.authenticationURL", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.validAbsoluteHttpURL(paramGenericConnectorConfiguration.authenticationURL, "connector.authenticationURL", new Object[0]))
      
      .valid((genericConnectorConfiguration.connectTimeout > 0), "connector.connectTimeout", new Object[0])
      .valid((genericConnectorConfiguration.readTimeout > 0), "connector.readTimeout", new Object[0])

      
      .ifTrue((genericConnectorConfiguration.sslCertificateKeyId != null), paramValidator -> paramValidator.valid((paramKey != null && paramKey.certificate != null), "connector.sslCertificateKey", new Object[] { paramGenericConnectorConfiguration.sslCertificateKeyId })).done();
  }
}
