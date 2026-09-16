package io.fusionauth.api.service.lambda.graal.domain;

import io.fusionauth.api.service.system.KeyReaderService;

public class GraalServicesObject extends AbstractProxyObject {
  public GraalServicesObject(KeyReaderService paramKeyReaderService) {
    this.members.put("secrets", new GraalSecretsObject(paramKeyReaderService));
  }
}
