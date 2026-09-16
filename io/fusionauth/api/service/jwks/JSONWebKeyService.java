package io.fusionauth.api.service.jwks;

import io.fusionauth.domain.jwks.JSONWebKeyInfoProvider;
import java.util.List;

public interface JSONWebKeyService {
  List<JSONWebKeyInfoProvider> retrieveAllProviders();
}
