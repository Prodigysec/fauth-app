package io.fusionauth.api.service.lambda.graal.domain;

import io.fusionauth.api.service.system.KeyReaderService;

public class GraalContextObject extends AbstractProxyObject {
  public GraalContextObject(KeyReaderService paramKeyReaderService) {
    this.members.put("services", new GraalServicesObject(paramKeyReaderService));
  }
}
