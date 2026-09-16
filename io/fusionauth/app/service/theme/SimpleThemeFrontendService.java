package io.fusionauth.app.service.theme;

import com.inversoft.util.Pair;
import java.util.List;

public interface SimpleThemeFrontendService {
  Pair<String, List<Integer>> mergeMessages(String paramString1, String paramString2);
}
