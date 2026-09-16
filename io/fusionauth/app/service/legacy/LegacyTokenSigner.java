package io.fusionauth.app.service.legacy;

import io.fusionauth.jwt.domain.JWT;
import java.util.UUID;

public interface LegacyTokenSigner {
  String sign(JWT paramJWT, String paramString1, UUID paramUUID, String paramString2, boolean paramBoolean);
}
