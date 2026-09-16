package io.fusionauth.api.service.ip;

import io.fusionauth.domain.Location;

public interface LocationService {
  Location ipToLocation(String paramString);
}
