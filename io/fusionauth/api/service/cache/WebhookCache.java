package io.fusionauth.api.service.cache;

import com.inversoft.cache.SimpleCache;
import io.fusionauth.domain.Webhook;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WebhookCache extends SimpleCache<UUID, Webhook> {
  public List<Webhook> retrieveForTenantAndGlobals(UUID paramUUID) {
    ArrayList<Webhook> arrayList = new ArrayList();
    for (Webhook webhook : this.cache.values()) {
      if (webhook.global) {
        arrayList.add(webhook);
        continue;
      } 
      if (paramUUID != null && webhook.tenantIds.contains(paramUUID))
        arrayList.add(webhook); 
    } 
    return arrayList;
  }
}
