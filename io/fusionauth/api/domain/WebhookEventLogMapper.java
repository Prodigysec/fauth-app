package io.fusionauth.api.domain;

import io.fusionauth.domain.WebhookEventLog;
import io.fusionauth.domain.search.WebhookEventLogSearchCriteria;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface WebhookEventLogMapper {
  void create(WebhookEventLog paramWebhookEventLog);
  
  int deleteByIds(@Param("ids") List<UUID> paramList);
  
  List<WebhookEventLog> retrieveAll();
  
  List<WebhookEventLog> retrieveByCriteria(WebhookEventLogSearchCriteria paramWebhookEventLogSearchCriteria);
  
  WebhookEventLog retrieveById(@Param("id") UUID paramUUID);
  
  int retrieveCountByCriteria(WebhookEventLogSearchCriteria paramWebhookEventLogSearchCriteria);
  
  @Select({"SELECT id FROM webhook_event_logs WHERE insert_instant < #{cutoff} LIMIT #{limit}"})
  List<UUID> retrieveIdsCreatedBeforeCutoff(@Param("cutoff") ZonedDateTime paramZonedDateTime, @Param("limit") int paramInt);
  
  int updateStatus(WebhookEventLog paramWebhookEventLog);
}
