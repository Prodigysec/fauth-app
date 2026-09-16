package io.fusionauth.api.service.lambda.graal.domain;

import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.domain.Key;
import org.graalvm.polyglot.Value;

public class GraalSecretsObject extends AbstractProxyObject {
  public GraalSecretsObject(KeyReaderService paramKeyReaderService) {
    this.members.put("get", paramArrayOfValue -> {
          if (paramArrayOfValue.length == 1 && !paramArrayOfValue[0].isNull()) {
            Key key = paramKeyReaderService.retrieveByKid(paramArrayOfValue[0].asString());
            return (key != null && key.type == Key.KeyType.Secret) ? key.secret : null;
          } 
          return null;
        });
  }
}
