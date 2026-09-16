package io.fusionauth.api.service.lambda;

import io.fusionauth.api.domain.CompiledLambda;
import io.fusionauth.api.domain.api.service.LambdaArgument;

public interface LambdaEngine {
  CompiledLambda.InvocationResult invoke(CompiledLambda paramCompiledLambda, LambdaArgument... paramVarArgs);
}
