package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.error.Errors;
import com.inversoft.jdbc.CurrentDatabaseEngine;
import com.inversoft.jdbc.Select;
import com.inversoft.sql.WhereBuilder;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.AuditLogMapper;
import io.fusionauth.api.domain.event.TenantAuditLogCreateEvent;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.util.DownloadTools;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.event.AuditLogCreateEvent;
import io.fusionauth.domain.search.AuditLogSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.commons.csv.CSVPrinter;
import org.mybatis.guice.transactional.Transactional;

public class DefaultAuditService implements AuditService {
  private final AuditLogMapper auditLogMapper;
  
  private final DataSource dataSource;
  
  private final TenantCache tenantCache;
  
  private final TenantReaderService tenantReader;
  
  @Inject
  public DefaultAuditService(AuditLogMapper paramAuditLogMapper, @Named("primary") DataSource paramDataSource, TenantCache paramTenantCache, TenantReaderService paramTenantReaderService) {
    this.auditLogMapper = paramAuditLogMapper;
    this.dataSource = paramDataSource;
    this.tenantCache = paramTenantCache;
    this.tenantReader = paramTenantReaderService;
  }
  
  @Transactional
  public void create(AuditLog paramAuditLog, EventInfo paramEventInfo) {
    paramAuditLog.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.auditLogMapper.create(paramAuditLog);
    if (paramAuditLog.tenantId != null) {
      Objects.requireNonNull(this.tenantReader);
      Tenant tenant = this.tenantCache.get(paramAuditLog.tenantId, this.tenantReader::retrieveById);
      EventHelper.send(tenant, null, new TenantAuditLogCreateEvent(paramEventInfo, paramAuditLog));
    } else {
      EventHelper.send(null, null, new AuditLogCreateEvent(paramEventInfo, paramAuditLog));
    } 
  }
  
  public void exportSearchResults(OutputStream paramOutputStream, AuditLogSearchCriteria paramAuditLogSearchCriteria, String paramString, ZoneId paramZoneId) {
    Objects.requireNonNull(paramString);
    Objects.requireNonNull(paramZoneId);
    (new DownloadTools.CSVOutputStreamWriter(paramOutputStream, "audit_log.csv"))
      .withExceptionObserver(paramException -> EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to export [audit_log.csv]", paramException)))
      .withHeaders(new String[] { "Id", "Tenant Id", "Created", "User", "Reason", "Message", "Old value", "New value" }).write(paramCSVPrinter -> {
          WhereBuilder whereBuilder = new WhereBuilder();
          whereBuilder.append("insert_user LIKE ?", paramAuditLogSearchCriteria.user).append("LOWER(message) LIKE ?", paramAuditLogSearchCriteria.message).append("tenants_id = ?", paramAuditLogSearchCriteria.tenantId).append("insert_instant >= ?", paramAuditLogSearchCriteria.start).append("insert_instant <= ?", paramAuditLogSearchCriteria.end);
          Connection connection = this.dataSource.getConnection();
          try {
            connection.setAutoCommit(false);
            ((Select)(new Select(connection)).in(String.format("SELECT id, tenants_id, insert_instant, insert_user, message, data FROM audit_logs %s ORDER BY insert_instant DESC", new Object[] { whereBuilder })).with(whereBuilder.getArgs())).stream().go(());
            connection.commit();
            if (connection != null)
              connection.close(); 
          } catch (Throwable throwable) {
            if (connection != null)
              try {
                connection.close();
              } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
              }  
            throw throwable;
          } 
        });
  }
  
  public AuditLog retrieveById(UUID paramUUID, int paramInt) {
    AuditLog auditLog = this.auditLogMapper.retrieveById(paramUUID, paramInt);
    if (auditLog == null)
      throw new NotFoundException(); 
    return auditLog;
  }
  
  public SearchResults<AuditLog> search(AuditLogSearchCriteria paramAuditLogSearchCriteria) {
    int i = this.auditLogMapper.retrieveCountByCriteria(paramAuditLogSearchCriteria);
    List<AuditLog> list = (i > 0) ? this.auditLogMapper.retrieveByCriteria(paramAuditLogSearchCriteria) : List.of();
    return new SearchResults<>(list, i);
  }
  
  public Errors validate(AuditLog paramAuditLog) {
    return (new Validator())
      .notBlank(paramAuditLog.insertUser, "auditLog.insertUser", new Object[0])
      .notBlank(paramAuditLog.message, "auditLog.message", new Object[0])
      .done();
  }
  
  private void printRecord(CSVPrinter paramCSVPrinter, ResultSet paramResultSet, ZoneId paramZoneId, String paramString) throws IOException, SQLException {
    try {
      String str = paramResultSet.getString("data");
      AuditLog auditLog = new AuditLog();
      auditLog.setDataFromDatabase(str);
      UUID uUID = null;
      if (CurrentDatabaseEngine.current == CurrentDatabaseEngine.DatabaseEngine.mysql) {
        byte[] arrayOfByte = paramResultSet.getBytes("tenants_id");
        if (arrayOfByte != null) {
          ByteBuffer byteBuffer = ByteBuffer.wrap(arrayOfByte);
          uUID = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
        } 
      } else {
        uUID = paramResultSet.<UUID>getObject("tenants_id", UUID.class);
      } 
      paramCSVPrinter.printRecord(new Object[] { Integer.valueOf(paramResultSet.getInt("id")), uUID, 
            
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(paramResultSet.getLong("insert_instant")), ZoneOffset.UTC)
            .withZoneSameInstant(paramZoneId).format(DateTimeFormatter.ofPattern(paramString)), paramResultSet
            .getString("insert_user"), auditLog.reason, paramResultSet
            
            .getString("message"), auditLog.oldValue, auditLog.newValue });
    } catch (NoSuchFieldException|IllegalAccessException noSuchFieldException) {
      throw new RuntimeException(noSuchFieldException);
    } 
  }
}
