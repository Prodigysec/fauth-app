package io.fusionauth.api.service.lambda.graal.domain;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyInstantiable;

public class Headers extends HeadersObject implements ProxyInstantiable {
  public Object newInstance(Value... paramVarArgs) {
    Headers headers = new Headers();
    if (paramVarArgs.length > 0) {
      Value value = paramVarArgs[0];
      if (value.hasArrayElements()) {
        for (byte b = 0; b < value.getArraySize(); b++) {
          Value value1 = value.getArrayElement(b);
          if (value1.hasArrayElements())
            headers.putMember(value1.getArrayElement(0L).asString(), value1.getArrayElement(1L)); 
        } 
      } else {
        for (String str : value.getMemberKeys())
          headers.putMember(str, value.getMember(str)); 
      } 
    } 
    return headers;
  }
}
