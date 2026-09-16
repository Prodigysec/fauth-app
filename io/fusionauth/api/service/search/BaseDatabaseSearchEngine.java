package io.fusionauth.api.service.search;

import com.inversoft.error.Errors;
import io.fusionauth.api.domain.SearchEngineResult;
import io.fusionauth.api.service.user.SearchByQueryUnsupportedException;
import io.fusionauth.domain.search.SortField;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class BaseDatabaseSearchEngine {
  public String createPointInTime() {
    return null;
  }
  
  public void delete(UUID paramUUID) {}
  
  public void deleteAll() {}
  
  public void deleteByIds(List<UUID> paramList) {}
  
  public void deleteIndexAndRecreate() {}
  
  public void flush() {}
  
  public String getIndexRefreshInterval() {
    return "60s";
  }
  
  public void setIndexRefreshInterval(String paramString) {}
  
  public void refresh() {}
  
  public SearchEngineResult searchByQuery(UUID paramUUID, String paramString1, int paramInt1, int paramInt2, List<SortField> paramList, boolean paramBoolean, List<String> paramList1, String paramString2) {
    throw new SearchByQueryUnsupportedException();
  }
  
  public SearchEngineResult searchByQueryString(UUID paramUUID, String paramString1, int paramInt1, int paramInt2, List<SortField> paramList, boolean paramBoolean, List<String> paramList1, String paramString2) {
    paramString1 = paramString1.replace("*", "%").toLowerCase();
    paramString1 = paramString1.startsWith("%") ? paramString1 : ("%" + paramString1);
    paramString1 = paramString1.endsWith("%") ? paramString1 : (paramString1 + "%");
    long l = searchByTermCount(paramUUID, paramString1);
    List<UUID> list = searchByTerm(paramUUID, paramString1, paramInt2, paramInt1, paramList);
    return new SearchEngineResult(list, true, l, null);
  }
  
  public SearchEngine.SearchEngineStatus status() {
    return null;
  }
  
  public Errors validate(UUID paramUUID, String paramString1, String paramString2, List<SortField> paramList, int paramInt1, int paramInt2) {
    Errors errors = new Errors();
    if (paramList == null)
      return errors; 
    Map<String, String> map = fieldMappings();
    for (byte b = 0; b < paramList.size(); b++) {
      SortField sortField = paramList.get(b);
      String str = map.get(sortField.name);
      if (str == null) {
        String str1 = map.keySet().stream().sorted().collect(Collectors.joining(", "));
        errors.addFieldError("sortFields[" + b + "].name", "[invalid]sortFields.name", null, new Object[] { sortField.name, str1 });
      } 
    } 
    return errors;
  }
  
  protected abstract Map<String, String> fieldMappings();
  
  protected abstract List<UUID> searchByTerm(UUID paramUUID, String paramString, int paramInt1, int paramInt2, List<SortField> paramList);
  
  protected abstract long searchByTermCount(UUID paramUUID, String paramString);
}
