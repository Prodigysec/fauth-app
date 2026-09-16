package io.fusionauth.api.domain;

import io.fusionauth.domain.WebAuthnCredential;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface WebAuthnCredentialMapper {
  void create(@Param("credential") WebAuthnCredential paramWebAuthnCredential);
  
  void createBulk(@Param("credentials") Collection<WebAuthnCredential> paramCollection);
  
  @Delete({"DELETE FROM webauthn_credentials WHERE id = #{id}"})
  void delete(UUID paramUUID);
  
  void deleteByUserId(@Param("userId") UUID paramUUID);
  
  Integer existsById(@Param("tenantId") UUID paramUUID1, @Param("id") UUID paramUUID2);
  
  WebAuthnCredential retrieveByCredentialId(@Param("tenantId") UUID paramUUID1, @Param("userId") UUID paramUUID2, @Param("credentialId") String paramString);
  
  WebAuthnCredential retrieveById(@Param("tenantId") UUID paramUUID1, @Param("id") UUID paramUUID2);
  
  List<WebAuthnCredential> retrieveByUserId(@Param("tenantId") UUID paramUUID1, @Param("userId") UUID paramUUID2);
  
  int update(WebAuthnCredential paramWebAuthnCredential);
}
