package io.fusionauth.api.service.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.inversoft.error.Errors;
import io.fusionauth.api.domain.SearchEngineResult;
import io.fusionauth.domain.search.SortField;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SearchEngine<T> {
  String createPointInTime();
  
  void delete(UUID paramUUID);
  
  void deleteAll();
  
  void deleteByIds(List<UUID> paramList);
  
  void deleteIndexAndRecreate();
  
  void flush();
  
  String getIndexRefreshInterval();
  
  void setIndexRefreshInterval(String paramString);
  
  void index(Collection<T> paramCollection);
  
  void refresh();
  
  SearchEngineResult searchByQuery(UUID paramUUID, String paramString1, int paramInt1, int paramInt2, List<SortField> paramList, boolean paramBoolean, List<String> paramList1, String paramString2);
  
  SearchEngineResult searchByQueryString(UUID paramUUID, String paramString1, int paramInt1, int paramInt2, List<SortField> paramList, boolean paramBoolean, List<String> paramList1, String paramString2);
  
  SearchEngineStatus status();
  
  Errors validate(UUID paramUUID, String paramString1, String paramString2, List<SortField> paramList, int paramInt1, int paramInt2);
  
  public static class SearchEngineStatus {
    public JsonNode cluster;
  }
}
