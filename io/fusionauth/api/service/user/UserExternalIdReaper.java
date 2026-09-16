package io.fusionauth.api.service.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.ExternalIdentifierMapper;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.app.service.MasterNodeRunnable;
import io.fusionauth.domain.Tenant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserExternalIdReaper extends MasterNodeRunnable {
  private static final Logger logger = LoggerFactory.getLogger(UserExternalIdReaper.class);
  
  private final ExternalIdentifierMapper backgroundExternalIdentifierMapper;
  
  private final TenantMapper backgroundTenantMapper;
  
  @Inject
  public UserExternalIdReaper(NodeService paramNodeService, @Named("background") ExternalIdentifierMapper paramExternalIdentifierMapper, @Named("background") TenantMapper paramTenantMapper) {
    super(paramNodeService);
    this.backgroundExternalIdentifierMapper = paramExternalIdentifierMapper;
    this.backgroundTenantMapper = paramTenantMapper;
  }
  
  public void runScheduled() {
    List<Tenant> list = this.backgroundTenantMapper.retrieveAll();
    for (Tenant tenant : list) {
      for (ExternalIdentifier.ExternalIdType externalIdType : ExternalIdentifier.ExternalIdType.values()) {
        int j = ExternalIdentifier.getTTLForReaping(externalIdType, tenant);
        if (j != -1) {
          long l3 = System.currentTimeMillis();
          long l4 = l3 - TimeUnit.SECONDS.toMillis(j);
          int k = this.backgroundExternalIdentifierMapper.deleteOlderThan(tenant.id, externalIdType, l4);
          long l5 = System.currentTimeMillis();
          logger.debug("Cleared [{}] expired [{}] Ids. Completed in {} milliseconds.", new Object[] { Integer.valueOf(k), externalIdType, Long.valueOf(l5 - l3) });
        } 
      } 
    } 
    long l1 = System.currentTimeMillis();
    int i = this.backgroundExternalIdentifierMapper.deleteExpired(ZonedDateTime.now(ZoneOffset.UTC));
    long l2 = System.currentTimeMillis();
    logger.debug("Cleared [{}] expired Ids. Completed in {} milliseconds.", Integer.valueOf(i), Long.valueOf(l2 - l1));
  }
}
