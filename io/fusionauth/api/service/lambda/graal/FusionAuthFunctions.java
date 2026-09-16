package io.fusionauth.api.service.lambda.graal;

import io.fusionauth.api.service.lambda.LambdaFunctions;
import io.fusionauth.api.service.lambda.graal.domain.AbstractProxyObject;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

public class FusionAuthFunctions extends AbstractProxyObject {
  public FusionAuthFunctions() {
    this.members.put("ActiveDirectory", new GraalActiveDirectoryFunctions());
  }
  
  public static class GraalActiveDirectoryFunctions extends AbstractProxyObject {
    public GraalActiveDirectoryFunctions() {
      this.members.put("b64GuidToString", new FusionAuthFunctions.GraalEncodedGuidToString());
    }
  }
  
  public static class GraalEncodedGuidToString implements ProxyExecutable {
    public Object execute(Value... param1VarArgs) {
      if (param1VarArgs.length == 1)
        return LambdaFunctions.encodedGuidToString(param1VarArgs[0].asString()); 
      return null;
    }
  }
}
