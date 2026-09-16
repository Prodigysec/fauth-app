package io.fusionauth.api.service.lambda;

import io.fusionauth.api.domain.api.service.LambdaArgument;
import java.util.UUID;

public interface LambdaInvocationService {
  void invoke(UUID paramUUID, LambdaArgument... paramVarArgs);
}
