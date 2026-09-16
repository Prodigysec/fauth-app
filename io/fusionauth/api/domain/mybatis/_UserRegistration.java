package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.UserRegistration;
import java.util.UUID;

public class _UserRegistration extends UserRegistration {
  @JsonIgnore
  public UUID userId;
}
