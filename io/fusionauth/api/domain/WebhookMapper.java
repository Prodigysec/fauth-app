package io.fusionauth.api.domain;

import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.search.WebhookSearchCriteria;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface WebhookMapper {
  void create(Webhook paramWebhook);
  
  void createAssociationsToTenants(@Param("id") UUID paramUUID, @Param("tenantIds") List<UUID> paramList);
  
  void createAssociationsToWebhooks(@Param("tenantId") UUID paramUUID, @Param("webhookIds") List<UUID> paramList);
  
  @Delete({"DELETE FROM webhooks WHERE id = #{id}"})
  int delete(UUID paramUUID);
  
  @Delete({"DELETE FROM webhooks_tenants WHERE tenants_id = #{tenantId}"})
  int deleteAssociationsByTenantId(UUID paramUUID);
  
  @Delete({"DELETE FROM webhooks_tenants WHERE webhooks_id = #{webhooksId}"})
  int deleteAssociationsByWebhookId(UUID paramUUID);
  
  List<Webhook> retrieveAll();
  
  List<Webhook> retrieveAllWithAssignedSslCertificate();
  
  List<Webhook> retrieveByCriteria(WebhookSearchCriteria paramWebhookSearchCriteria);
  
  Webhook retrieveById(@Param("id") UUID paramUUID);
  
  Webhook retrieveByURL(@Param("url") URI paramURI);
  
  int retrieveCountByCriteria(WebhookSearchCriteria paramWebhookSearchCriteria);
  
  int update(Webhook paramWebhook);
}
