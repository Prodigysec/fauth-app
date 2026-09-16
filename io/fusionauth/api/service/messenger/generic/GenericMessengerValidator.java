package io.fusionauth.api.service.messenger.generic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.messenger.MessengerValidator;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.GenericMessengerConfiguration;

public class GenericMessengerValidator implements MessengerValidator {
  private final ObjectMapper objectMapper;
  
  @Inject
  public GenericMessengerValidator(ObjectMapper paramObjectMapper) {
    this.objectMapper = paramObjectMapper;
  }
  
  public Errors validate(BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    GenericMessengerConfiguration genericMessengerConfiguration = (GenericMessengerConfiguration)paramBaseMessengerConfiguration;
    return (new Validator(this.objectMapper))
      
      .notMissing(genericMessengerConfiguration.url, "messenger.url", new Object[0])

      
      .ifLastCheckHadNoError(paramValidator -> paramValidator.validAbsoluteHttpURL(paramGenericMessengerConfiguration.url, "messenger.url", new Object[] { paramGenericMessengerConfiguration.url })).maxLength(genericMessengerConfiguration.httpAuthenticationPassword, 255, "messenger.httpAuthenticationPassword", new Object[] { Integer.valueOf(255) }).maxLength(genericMessengerConfiguration.httpAuthenticationUsername, 255, "messenger.httpAuthenticationUsername", new Object[] { Integer.valueOf(255) }).notMissing(genericMessengerConfiguration.readTimeout, "messenger.readTimeout", new Object[0])
      .notMissing(genericMessengerConfiguration.connectTimeout, "messenger.connectTimeout", new Object[0])

      
      .ifTrue((genericMessengerConfiguration.readTimeout != null), paramValidator -> paramValidator.valid((paramGenericMessengerConfiguration.readTimeout.intValue() > 0), "messenger.readTimeout", new Object[0]))
      
      .ifTrue((genericMessengerConfiguration.connectTimeout != null), paramValidator -> paramValidator.valid((paramGenericMessengerConfiguration.connectTimeout.intValue() > 0), "messenger.connectTimeout", new Object[0]))


      
      .notEmpty(genericMessengerConfiguration.messageTypes, "messenger.messageTypes", new Object[0])
      
      .done();
  }
}
