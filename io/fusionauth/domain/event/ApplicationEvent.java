package io.fusionauth.domain.event;

import java.util.Collection;
import java.util.UUID;

public interface ApplicationEvent {
  Collection<UUID> applicationIds();
}
