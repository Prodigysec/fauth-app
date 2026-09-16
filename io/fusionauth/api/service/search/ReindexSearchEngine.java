package io.fusionauth.api.service.search;

public interface ReindexSearchEngine<T> extends SearchEngine<T> {
  String createIndex();
  
  void deleteIndex(String paramString);
  
  void updateAliasDeleteOldIndex();
}
