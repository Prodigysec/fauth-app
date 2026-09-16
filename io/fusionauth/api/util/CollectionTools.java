package io.fusionauth.api.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionTools {
  @SafeVarargs
  public static <T> Set<T> concatSet(Collection<T> paramCollection, T... paramVarArgs) {
    return (Set<T>)Stream.concat(paramCollection
        .stream(), 
        Stream.of((Object[])paramVarArgs))
      .collect(Collectors.toUnmodifiableSet());
  }
  
  public static <V> List<V> readOnly(List<V> paramList) {
    // Byte code:
    //   0: new java/util/ArrayList
    //   3: dup
    //   4: invokespecial <init> : ()V
    //   7: astore_1
    //   8: aload_0
    //   9: invokeinterface iterator : ()Ljava/util/Iterator;
    //   14: astore_2
    //   15: aload_2
    //   16: invokeinterface hasNext : ()Z
    //   21: ifeq -> 98
    //   24: aload_2
    //   25: invokeinterface next : ()Ljava/lang/Object;
    //   30: astore_3
    //   31: aload_3
    //   32: instanceof java/util/List
    //   35: ifeq -> 59
    //   38: aload_3
    //   39: checkcast java/util/List
    //   42: astore #4
    //   44: aload_1
    //   45: aload #4
    //   47: invokestatic readOnly : (Ljava/util/List;)Ljava/util/List;
    //   50: invokeinterface add : (Ljava/lang/Object;)Z
    //   55: pop
    //   56: goto -> 95
    //   59: aload_3
    //   60: instanceof java/util/Map
    //   63: ifeq -> 87
    //   66: aload_3
    //   67: checkcast java/util/Map
    //   70: astore #5
    //   72: aload_1
    //   73: aload #5
    //   75: invokestatic readOnly : (Ljava/util/Map;)Ljava/util/Map;
    //   78: invokeinterface add : (Ljava/lang/Object;)Z
    //   83: pop
    //   84: goto -> 95
    //   87: aload_1
    //   88: aload_3
    //   89: invokeinterface add : (Ljava/lang/Object;)Z
    //   94: pop
    //   95: goto -> 15
    //   98: aload_1
    //   99: invokestatic unmodifiableList : (Ljava/util/List;)Ljava/util/List;
    //   102: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #44	-> 0
    //   #46	-> 8
    //   #47	-> 31
    //   #49	-> 44
    //   #50	-> 59
    //   #52	-> 72
    //   #54	-> 87
    //   #56	-> 95
    //   #58	-> 98
  }
  
  public static <K, V> Map<K, V> readOnly(Map<K, V> paramMap) {
    LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
    for (Map.Entry<K, V> entry : paramMap.entrySet()) {
      Object object = entry.getValue();
      if (object instanceof List) {
        List<?> list = (List)object;
        linkedHashMap.put(entry.getKey(), readOnly(list));
        continue;
      } 
      object = entry.getValue();
      if (object instanceof Map) {
        Map<?, ?> map = (Map)object;
        linkedHashMap.put(entry.getKey(), readOnly(map));
        continue;
      } 
      linkedHashMap.put(entry.getKey(), entry.getValue());
    } 
    return Collections.unmodifiableMap((Map)linkedHashMap);
  }
}
