package io.fusionauth.domain.event;

import java.util.UUID;

public interface ObjectIdentifiable {
  UUID getLinkedObjectId();
  
  void setLinkedObjectId(UUID paramUUID);
}
