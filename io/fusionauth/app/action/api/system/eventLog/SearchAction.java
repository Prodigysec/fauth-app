package io.fusionauth.app.action.api.system.eventLog;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.system.EventLogService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.api.EventLogSearchRequest;
import io.fusionauth.domain.api.EventLogSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.EventLogSearchCriteria;
import java.time.ZonedDateTime;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class SearchAction extends BaseSearchAPIAction<EventLogSearchCriteria> {
  @JSONRequest
  public final EventLogSearchRequest request = new EventLogSearchRequest();
  
  private final EventLogService eventLogService;
  
  @JSONResponse
  public EventLogSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, EventLogService paramEventLogService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.eventLogService = paramEventLogService;
  }
  
  public ZonedDateTime getEnd() {
    return (criteria()).end;
  }
  
  public void setEnd(ZonedDateTime paramZonedDateTime) {
    (criteria()).end = paramZonedDateTime;
  }
  
  public String getMessage() {
    return (criteria()).message;
  }
  
  public void setMessage(String paramString) {
    (criteria()).message = paramString;
  }
  
  public ZonedDateTime getStart() {
    return (criteria()).start;
  }
  
  public void setStart(ZonedDateTime paramZonedDateTime) {
    (criteria()).start = paramZonedDateTime;
  }
  
  public EventLogType getType() {
    return (criteria()).type;
  }
  
  public void setType(EventLogType paramEventLogType) {
    (criteria()).type = paramEventLogType;
  }
  
  protected EventLogSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    this.response = new EventLogSearchResponse(this.eventLogService.search(this.request.search));
    return "render";
  }
}
