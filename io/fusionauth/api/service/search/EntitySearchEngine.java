package io.fusionauth.api.service.search;

import io.fusionauth.domain.Entity;
import java.util.UUID;

public interface EntitySearchEngine extends SearchEngine<Entity> {
  void deleteByType(UUID paramUUID);
}
