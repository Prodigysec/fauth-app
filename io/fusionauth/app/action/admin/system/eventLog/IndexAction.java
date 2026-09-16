package io.fusionauth.app.action.admin.system.eventLog;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.api.EventLogSearchRequest;
import io.fusionauth.domain.api.EventLogSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.EventLogSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "event_log_viewer"})
public class IndexAction extends BaseSearchAction<EventLog, EventLogSearchCriteria> {
  @FTLVariable
  public List<EventLogType> eventLogTypes = new ArrayList<>(
      Arrays.asList(new EventLogType[] { EventLogType.Information, EventLogType.Debug, EventLogType.Error }));
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected EventLogSearchCriteria defaultSearchCriteria() {
    return new EventLogSearchCriteria();
  }
  
  protected SearchResults<EventLog> search() {
    EventLogSearchResponse eventLogSearchResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchEventLogs(new EventLogSearchRequest(this.s)));
    return new SearchResults<>(eventLogSearchResponse.eventLogs, eventLogSearchResponse.total);
  }
}
