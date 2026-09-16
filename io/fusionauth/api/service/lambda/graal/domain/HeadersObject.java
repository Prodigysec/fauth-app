package io.fusionauth.api.service.lambda.graal.domain;

import com.inversoft.json.ToString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;

public class HeadersObject extends AbstractProxyObject {
  public HeadersObject(Map<String, List<String>> paramMap) {
    for (String str : paramMap.keySet()) {
      if (str != null)
        this.members.put(str, ProxyArray.fromList(new ArrayList(paramMap.get(str)))); 
    } 
  }
  
  public HeadersObject() {}
  
  public Object getMember(String paramString) {
    if (paramString == null)
      return null; 
    for (String str : this.members.keySet()) {
      if (str != null && 
        str.equalsIgnoreCase(paramString)) {
        ProxyArray proxyArray = (ProxyArray)this.members.get(str);
        return (proxyArray.getSize() == 1L) ? proxyArray.get(0L) : proxyArray;
      } 
    } 
    return null;
  }
  
  public boolean hasMember(String paramString) {
    return (getMember(paramString) != null);
  }
  
  public void putMember(String paramString, Value paramValue) {
    Value value = paramValue.getContext().asValue(this.members.get(paramString));
    if (value.isNull()) {
      this.members.put(paramString, ProxyArray.fromArray(new Object[] { paramValue }));
    } else {
      ArrayList<Value> arrayList = new ArrayList((Collection)value.as(List.class));
      arrayList.add(paramValue);
      this.members.put(paramString, ProxyArray.fromList(arrayList));
    } 
  }
  
  public String toString() {
    LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
    this.members.forEach((paramString, paramObject) -> {
          StringBuilder stringBuilder = new StringBuilder();
          if (paramObject instanceof ProxyArray) {
            ProxyArray proxyArray = (ProxyArray)paramObject;
            for (byte b = 0; b < proxyArray.getSize(); b++) {
              stringBuilder.append(proxyArray.get(b).toString());
              if (b < proxyArray.getSize() - 1L)
                stringBuilder.append(","); 
            } 
          } else {
            stringBuilder.append(((Value)paramObject).asString());
          } 
          ((List<String>)paramMap.computeIfAbsent(paramString, ())).add(stringBuilder.toString());
        });
    for (String str : linkedHashMap.keySet()) {
      Object object = linkedHashMap.get(str);
      if (object instanceof List) {
        List list = (List)object;
        if (list.size() != 0)
          linkedHashMap.put(str, list.get(0)); 
      } 
    } 
    return ToString.toJSONString(linkedHashMap);
  }
}
