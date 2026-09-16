package io.fusionauth.app.service.theme;

import com.inversoft.util.Pair;
import io.fusionauth.api.util.PropertiesTools;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class DefaultSimpleThemeFrontendService implements SimpleThemeFrontendService {
  public Pair<String, List<Integer>> mergeMessages(String paramString1, String paramString2) {
    try {
      Pair<Properties, List<Pair<String, String>>> pair = PropertiesTools.loadPropertiesAndMapKeys(paramString1);
      Properties properties1 = (Properties)pair.first;
      List<Pair> list = (List)pair.second;
      Properties properties2 = PropertiesTools.loadProperties(paramString2);
      ArrayList<Integer> arrayList = new ArrayList();
      ArrayList<String> arrayList1 = new ArrayList();
      for (byte b = 0; b < list.size(); b++) {
        Pair pair1 = list.get(b);
        String str1 = (String)pair1.first;
        String str2 = (String)pair1.second;
        if (str2 != null && properties2.containsKey(str2)) {
          arrayList.add(Integer.valueOf(b));
          str1 = str1.replace(properties1.getProperty(str2), (String)properties2.remove(str2));
        } 
        arrayList1.add(str1);
      } 
      if (!properties2.isEmpty()) {
        Objects.requireNonNull(arrayList1);
        "\n#\n# Custom messages\n#\n".lines().forEach(arrayList1::add);
        Objects.requireNonNull(arrayList1);
        properties2.entrySet().stream().map(paramEntry -> String.valueOf(paramEntry.getKey()) + "=" + String.valueOf(paramEntry.getKey())).forEach(arrayList1::add);
      } 
      String str = String.join("\n", (Iterable)arrayList1);
      return new Pair(str, arrayList);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    } 
  }
}
