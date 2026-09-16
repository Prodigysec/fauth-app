package io.fusionauth.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class MapperTools {
  public static int MaximumIndexedColumnLength = 191;
  
  public static <K, V> void safeCreateUpdate(int paramInt, Map<K, V> paramMap, Consumer<Map<K, V>> paramConsumer) {
    if (paramMap == null || paramMap.size() == 0)
      return; 
    ArrayList arrayList = new ArrayList(paramMap.keySet());
    int i;
    for (i = 0; i < arrayList.size(); i += paramInt) {
      int j = Math.min(i + paramInt, arrayList.size());
      List list = arrayList.subList(i, j);
      HashMap<Object, Object> hashMap = new HashMap<>(list.size());
      list.forEach(paramObject -> paramMap1.put(paramObject, paramMap2.get(paramObject)));
      paramConsumer.accept(hashMap);
    } 
  }
  
  public static <T> void safeCreateUpdate(int paramInt, Collection<T> paramCollection, Consumer<List<T>> paramConsumer) {
    if (paramCollection == null || paramCollection.isEmpty())
      return; 
    List<T> list = toList(paramCollection);
    int i;
    for (i = 0; i < list.size(); i += paramInt) {
      int j = Math.min(i + paramInt, list.size());
      List<T> list1 = list.subList(i, j);
      paramConsumer.accept(list1);
    } 
  }
  
  public static <T> int safeDelete(int paramInt, Collection<T> paramCollection, Function<List<T>, Integer> paramFunction) {
    if (paramCollection == null || paramCollection.size() == 0)
      return 0; 
    List<T> list = toList(paramCollection);
    int i = 0;
    int j;
    for (j = 0; j < list.size(); j += paramInt) {
      int k = Math.min(j + paramInt, list.size());
      List<T> list1 = list.subList(j, k);
      i += ((Integer)paramFunction.apply(list1)).intValue();
    } 
    return i;
  }
  
  public static <T, V> List<V> safeRetrieve(int paramInt, Collection<T> paramCollection, Function<List<T>, List<V>> paramFunction) {
    ArrayList<V> arrayList = new ArrayList();
    if (paramCollection == null || paramCollection.size() == 0)
      return arrayList; 
    List<T> list = toList(paramCollection);
    int i;
    for (i = 0; i < list.size(); i += paramInt) {
      int j = Math.min(i + paramInt, list.size());
      List<T> list1 = list.subList(i, j);
      arrayList.addAll(paramFunction.apply(list1));
    } 
    return arrayList;
  }
  
  private static <T> List<T> toList(Collection<T> paramCollection) {
    List<T> list = null;
    if (paramCollection instanceof List) {
      List<T> list1 = (List)paramCollection;
      list = list1;
    } else if (paramCollection instanceof Set) {
      Set<?> set = (Set)paramCollection;
      list = new LinkedList(set);
    } 
    if (list == null)
      throw new IllegalArgumentException("Unexpected collection type. Expected a List, or Set, but found [" + paramCollection.getClass().getSimpleName() + "]"); 
    return list;
  }
}
