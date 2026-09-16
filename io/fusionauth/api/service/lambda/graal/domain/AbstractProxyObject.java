package io.fusionauth.api.service.lambda.graal.domain;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;

public abstract class AbstractProxyObject implements ProxyObject {
  protected final Map<String, Object> members = new LinkedHashMap<>(1);
  
  public Object getMember(String paramString) {
    return this.members.get(paramString);
  }
  
  public Object getMemberKeys() {
    return ProxyArray.fromList(Arrays.asList(this.members.keySet().toArray()));
  }
  
  public boolean hasMember(String paramString) {
    return this.members.containsKey(paramString);
  }
  
  public void putMember(String paramString, Value paramValue) {}
}
