package io.fusionauth.api.service.authentication;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.domain.IntervalCount;
import io.fusionauth.api.domain.RegistrationCountMapper;
import io.fusionauth.api.domain.guice.mybatis.UseDataSource;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.api.util.MapperTools;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;
import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.guice.transactional.Transactional;

public class RegistrationCounterAggregator implements Runnable {
  private final RegistrationCountMapper backgroundRegistrationCountMapper;
  
  private final NodeService nodeService;
  
  @Inject
  public RegistrationCounterAggregator(@Named("background") RegistrationCountMapper paramRegistrationCountMapper, NodeService paramNodeService) {
    this.backgroundRegistrationCountMapper = paramRegistrationCountMapper;
    this.nodeService = paramNodeService;
  }
  
  @Transactional
  @UseDataSource("background")
  public void _applicationCounts() {
    List<IntervalCount> list = this.backgroundRegistrationCountMapper.retrieveRawApplication();
    if (list.size() > 0) {
      Objects.requireNonNull(this.backgroundRegistrationCountMapper);
      MapperTools.safeCreateUpdate(5000, list, this.backgroundRegistrationCountMapper::bulkUpsertApplication);
      OptionalLong optionalLong = list.stream().mapToLong(paramIntervalCount -> paramIntervalCount.id).max();
      this.backgroundRegistrationCountMapper.deleteRawApplicationWhereIdLessThanOrEqual(optionalLong.getAsLong());
    } 
  }
  
  @Transactional
  @UseDataSource("background")
  public void _globalCounts() {
    List<IntervalCount> list = this.backgroundRegistrationCountMapper.retrieveRawGlobal();
    if (list.size() > 0) {
      Objects.requireNonNull(this.backgroundRegistrationCountMapper);
      MapperTools.safeCreateUpdate(5000, list, this.backgroundRegistrationCountMapper::bulkUpsertGlobal);
      OptionalLong optionalLong = list.stream().mapToLong(paramIntervalCount -> paramIntervalCount.id).max();
      this.backgroundRegistrationCountMapper.deleteRawGlobalWhereIdLessThanOrEqual(optionalLong.getAsLong());
    } 
  }
  
  public void run() {
    if (this.nodeService.isMaster()) {
      _globalCounts();
      try {
        _applicationCounts();
      } catch (PersistenceException persistenceException) {
        this.backgroundRegistrationCountMapper.deleteOrphanedRawApplication();
        _applicationCounts();
      } 
    } 
  }
}
