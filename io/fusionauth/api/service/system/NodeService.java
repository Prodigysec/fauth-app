package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.AsyncTaskMapper;
import io.fusionauth.api.domain.FusionAuthNodeMapper;
import io.fusionauth.api.domain.MasterRecordMapper;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.domain.guice.mybatis.UseDataSource;
import io.fusionauth.api.util.NetworkTools;
import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeService implements Closeable {
  public static final long MASTER_RUN_INTERVAL = 173L;
  
  public static final int NODE_RUN_INTERVAL = 37;
  
  private static final Logger logger = LoggerFactory.getLogger(NodeService.class);
  
  private final AsyncTaskMapper backgroundAsyncTaskMapper;
  
  private final FusionAuthNodeMapper backgroundFusionAuthNodeMapper;
  
  private final MasterRecordMapper backgroundMasterRecordMapper;
  
  private final FusionAuthConfiguration configuration;
  
  @Inject
  public NodeService(@Named("background") AsyncTaskMapper paramAsyncTaskMapper, @Named("background") FusionAuthNodeMapper paramFusionAuthNodeMapper, @Named("background") MasterRecordMapper paramMasterRecordMapper, FusionAuthConfiguration paramFusionAuthConfiguration) {
    this.backgroundAsyncTaskMapper = paramAsyncTaskMapper;
    this.backgroundFusionAuthNodeMapper = paramFusionAuthNodeMapper;
    this.backgroundMasterRecordMapper = paramMasterRecordMapper;
    this.configuration = paramFusionAuthConfiguration;
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    List<FusionAuthNodeMapper.FusionAuthNode> list = paramFusionAuthNodeMapper.retrieveAll();
    deleteStaleNodes(list, zonedDateTime);
    RuntimeMode runtimeMode = list.isEmpty() ? paramFusionAuthConfiguration.runtimeMode() : ((FusionAuthNodeMapper.FusionAuthNode)list.get(0)).runtimeMode;
    if (!paramFusionAuthConfiguration.runtimeMode().equals(runtimeMode)) {
      logger.error("Node [{}] cannot be added. The runtime mode is [{}] and this node in [{}] mode.", new Object[] { FusionAuthNodeMapper.FusionAuthNode.ID, runtimeMode, paramFusionAuthConfiguration.runtimeMode() });
      System.exit(1);
    } 
    String str = paramFusionAuthConfiguration.appURL();
    logger.info("Node [{}] added with address [{}]", FusionAuthNodeMapper.FusionAuthNode.ID, str);
    paramFusionAuthNodeMapper.upsert(new FusionAuthNodeMapper.FusionAuthNode(FusionAuthNodeMapper.FusionAuthNode.ID, ZonedDateTime.now(ZoneOffset.UTC), NetworkTools.getIpv4HostAddresses(), paramFusionAuthConfiguration.runtimeMode(), str));
  }
  
  @Transactional
  @UseDataSource("background")
  public void _releaseMasterRecord() {
    MasterRecordMapper.MasterRecord masterRecord = this.backgroundMasterRecordMapper.retrieveAndLock();
    if (masterRecord.id.equals(FusionAuthNodeMapper.FusionAuthNode.ID))
      this.backgroundMasterRecordMapper.release(); 
  }
  
  @Transactional
  @UseDataSource("background")
  public void claimMasterRecord() {
    MasterRecordMapper.MasterRecord masterRecord = this.backgroundMasterRecordMapper.retrieveAndLock();
    if (!masterRecord.id.equals(FusionAuthNodeMapper.FusionAuthNode.ID)) {
      ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
      logger.info("Node [{}] forcibly promoted to master.", FusionAuthNodeMapper.FusionAuthNode.ID);
      masterRecord.id = FusionAuthNodeMapper.FusionAuthNode.ID;
      masterRecord.instant = zonedDateTime;
      this.backgroundMasterRecordMapper.claim(masterRecord);
    } 
  }
  
  public void close() {
    FusionAuthNodeMapper.FusionAuthNode fusionAuthNode = this.backgroundFusionAuthNodeMapper.retrieveById(FusionAuthNodeMapper.FusionAuthNode.ID);
    if (fusionAuthNode != null) {
      logger.info("Shut down. Node [{}] removed with address [{}]", FusionAuthNodeMapper.FusionAuthNode.ID, fusionAuthNode.url);
      deleteNode(fusionAuthNode);
      _releaseMasterRecord();
    } 
  }
  
  @Transactional
  @UseDataSource("background")
  public boolean isMaster() {
    boolean bool = false;
    MasterRecordMapper.MasterRecord masterRecord = this.backgroundMasterRecordMapper.retrieveAndLock();
    ZonedDateTime zonedDateTime1 = ZonedDateTime.now(ZoneOffset.UTC);
    ZonedDateTime zonedDateTime2 = zonedDateTime1.minusSeconds(346L);
    if (masterRecord.id.equals(FusionAuthNodeMapper.FusionAuthNode.ID)) {
      this.backgroundMasterRecordMapper.update(zonedDateTime1);
      bool = true;
    } else if (masterRecord.instant.isBefore(zonedDateTime2)) {
      if (masterRecord.id.equals(new UUID(0L, 0L))) {
        logger.info("Node [{}] promoted to master at [{}]", FusionAuthNodeMapper.FusionAuthNode.ID, zonedDateTime1);
      } else if (masterRecord.instant.toInstant().equals(Instant.EPOCH)) {
        logger.info("Node [{}] promoted to master at [{}], the previous master Node [{}] has been shutdown or removed", new Object[] { FusionAuthNodeMapper.FusionAuthNode.ID, zonedDateTime1, masterRecord.id });
      } else {
        logger.info("Node [{}] promoted to master at [{}], the previous master Node [{}] had not checked in since [{}]", new Object[] { FusionAuthNodeMapper.FusionAuthNode.ID, zonedDateTime1, masterRecord.id, masterRecord.instant });
      } 
      masterRecord.id = FusionAuthNodeMapper.FusionAuthNode.ID;
      masterRecord.instant = zonedDateTime1;
      this.backgroundMasterRecordMapper.claim(masterRecord);
      bool = true;
    } 
    return bool;
  }
  
  @Transactional
  @UseDataSource("background")
  public void reset() {
    this.backgroundFusionAuthNodeMapper.retrieveAll().forEach(this::deleteNode);
    this.backgroundFusionAuthNodeMapper.upsert(new FusionAuthNodeMapper.FusionAuthNode(FusionAuthNodeMapper.FusionAuthNode.ID, ZonedDateTime.now(ZoneOffset.UTC), NetworkTools.getIpv4HostAddresses(), this.configuration.runtimeMode(), this.configuration.appURL()));
  }
  
  public List<FusionAuthNodeMapper.FusionAuthNode> retrieveAll() {
    long l = System.currentTimeMillis();
    List<FusionAuthNodeMapper.FusionAuthNode> list = this.backgroundFusionAuthNodeMapper.retrieveAll();
    for (FusionAuthNodeMapper.FusionAuthNode fusionAuthNode : list) {
      fusionAuthNode.uptimeInDays = (int)TimeUnit.MILLISECONDS.toDays(l - fusionAuthNode.insertInstant.toInstant().toEpochMilli());
      if (fusionAuthNode.id.equals(FusionAuthNodeMapper.FusionAuthNode.ID))
        fusionAuthNode.me = true; 
    } 
    return list;
  }
  
  @Transactional
  @UseDataSource("background")
  public void update() {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    List<FusionAuthNodeMapper.FusionAuthNode> list = this.backgroundFusionAuthNodeMapper.retrieveAll();
    deleteStaleNodes(list, zonedDateTime);
    if (this.configuration.runtimeMode() == RuntimeMode.Testing) {
      this.backgroundFusionAuthNodeMapper.upsert(new FusionAuthNodeMapper.FusionAuthNode(FusionAuthNodeMapper.FusionAuthNode.ID, zonedDateTime, NetworkTools.getIpv4HostAddresses(), this.configuration.runtimeMode(), this.configuration.appURL()));
      return;
    } 
    String str = this.configuration.appURL();
    if (this.configuration.appURLComputed()) {
      String str1 = this.configuration.computeAppURL();
      if (!str1.equals(str)) {
        logger.info("Node [{}] address changed from [{}] to [{}]", new Object[] { FusionAuthNodeMapper.FusionAuthNode.ID, str, str1 });
        this.configuration.setAppURL(str1);
        str = str1;
      } 
    } 
    this.backgroundFusionAuthNodeMapper.upsert(new FusionAuthNodeMapper.FusionAuthNode(FusionAuthNodeMapper.FusionAuthNode.ID, zonedDateTime, NetworkTools.getIpv4HostAddresses(), this.configuration.runtimeMode(), str));
  }
  
  private void deleteNode(FusionAuthNodeMapper.FusionAuthNode paramFusionAuthNode) {
    this.backgroundAsyncTaskMapper.relinquishTasks(paramFusionAuthNode.id);
    this.backgroundFusionAuthNodeMapper.delete(paramFusionAuthNode.id);
  }
  
  private void deleteNodeWithLog(FusionAuthNodeMapper.FusionAuthNode paramFusionAuthNode) {
    Duration duration = Duration.between(paramFusionAuthNode.lastCheckinInstant, ZonedDateTime.now(ZoneOffset.UTC));
    logger.info("Node [{}] with address [{}] removed because it has not checked in for the last [{}] seconds. Bye node.", new Object[] { FusionAuthNodeMapper.FusionAuthNode.ID, paramFusionAuthNode.url, Long.valueOf(duration.getSeconds()) });
    deleteNode(paramFusionAuthNode);
  }
  
  private void deleteStaleNodes(List<FusionAuthNodeMapper.FusionAuthNode> paramList, ZonedDateTime paramZonedDateTime) {
    ZonedDateTime zonedDateTime = paramZonedDateTime.minusSeconds(74L);
    paramList.stream()
      .filter(paramFusionAuthNode -> paramFusionAuthNode.lastCheckinInstant.isBefore(paramZonedDateTime))
      .forEach(this::deleteNodeWithLog);
  }
  
  static {
    logger.info("Node [{}] started.", FusionAuthNodeMapper.FusionAuthNode.ID);
  }
}
