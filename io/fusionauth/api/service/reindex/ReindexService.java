package io.fusionauth.api.service.reindex;

import com.inversoft.error.Errors;

public interface ReindexService {
  boolean inProgress();
  
  Thread reindexEntities();
  
  Thread reindexUsers();
  
  Errors validate(String paramString);
}
