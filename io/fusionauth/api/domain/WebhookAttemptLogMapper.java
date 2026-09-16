package io.fusionauth.api.domain;

import io.fusionauth.domain.WebhookAttemptLog;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface WebhookAttemptLogMapper {
  void create(WebhookAttemptLog paramWebhookAttemptLog);
  
  @Delete({"DELETE FROM webhook_attempt_logs WHERE webhooks_id = #{webhookId}"})
  int deleteAttemptsByWebhookId(UUID paramUUID);
  
  int deleteByWebhookEventLogIds(@Param("webhookEventLogIds") List<UUID> paramList);
  
  List<WebhookAttemptLog> retrieveAll();
  
  WebhookAttemptLog retrieveById(UUID paramUUID);
  
  List<WebhookAttemptLog> retrieveByWebhookEventLogId(UUID paramUUID);
}
