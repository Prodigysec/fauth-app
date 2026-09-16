package io.fusionauth.api.service.system;

import com.inversoft.error.Errors;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.search.EventLogSearchCriteria;
import io.fusionauth.domain.search.SearchResults;

public interface EventLogService {
  void create(EventLog paramEventLog);
  
  void create(EventLog paramEventLog, boolean paramBoolean);
  
  void deleteOld();
  
  EventLog retrieveById(int paramInt);
  
  SearchResults<EventLog> search(EventLogSearchCriteria paramEventLogSearchCriteria);
  
  Errors validate(EventLog paramEventLog);
}
