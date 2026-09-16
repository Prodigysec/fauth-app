package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.EventLogMapper;
import io.fusionauth.api.domain.SystemConfigurationMapper;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.event.EventLogCreateEvent;
import io.fusionauth.domain.search.EventLogSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

public class DefaultEventLogService implements EventLogService {
  private final EventLogMapper backgroundEventLogMapper;
  
  private final SystemConfigurationMapper backgroundSystemConfigurationMapper;
  
  private final EventLogMapper eventLogMapper;
  
  private final EventLogMapper secondaryEventLogMapper;
  
  @Inject
  public DefaultEventLogService(@Named("background") EventLogMapper paramEventLogMapper1, @Named("background") SystemConfigurationMapper paramSystemConfigurationMapper, EventLogMapper paramEventLogMapper2, @Named("secondary") EventLogMapper paramEventLogMapper3) {
    this.backgroundEventLogMapper = paramEventLogMapper1;
    this.backgroundSystemConfigurationMapper = paramSystemConfigurationMapper;
    this.eventLogMapper = paramEventLogMapper2;
    this.secondaryEventLogMapper = paramEventLogMapper3;
  }
  
  public void create(EventLog paramEventLog) {
    create(paramEventLog, true);
  }
  
  public void create(EventLog paramEventLog, boolean paramBoolean) {
    paramEventLog.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    if (paramEventLog.type == null)
      paramEventLog.type = EventLogType.Error; 
    paramEventLog.message = paramEventLog.message.replace("\000", "");
    this.secondaryEventLogMapper.create(paramEventLog);
    if (paramBoolean)
      EventHelper.send(null, null, new EventLogCreateEvent(paramEventLog)); 
  }
  
  public void deleteOld() {
    int i = (this.backgroundSystemConfigurationMapper.retrieve()).eventLogConfiguration.numberToRetain;
    int j = this.backgroundEventLogMapper.count(new EventLogSearchCriteria());
    if (j <= i)
      return; 
    this.backgroundEventLogMapper.deleteOld(j - i);
  }
  
  public EventLog retrieveById(int paramInt) {
    EventLog eventLog = this.eventLogMapper.retrieveById(paramInt);
    if (eventLog == null)
      throw new NotFoundException(); 
    return eventLog;
  }
  
  public SearchResults<EventLog> search(EventLogSearchCriteria paramEventLogSearchCriteria) {
    int i = this.eventLogMapper.count(paramEventLogSearchCriteria);
    List<EventLog> list = (i > 0) ? this.eventLogMapper.retrieveByCriteria(paramEventLogSearchCriteria) : List.of();
    return new SearchResults<>(list, i);
  }
  
  public Errors validate(EventLog paramEventLog) {
    return (new Validator())
      .notBlank(paramEventLog.message, "eventLog.message", new Object[0])
      .done();
  }
}
