package io.fusionauth.api.migration.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.jdbc.Select;
import com.inversoft.migration.Migration;
import com.inversoft.sql.UUIDTools;
import io.fusionauth.api.service.identity.IdentityProviderReaderService;
import io.fusionauth.api.service.identity.IdentityProviderService;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.service.system.KeyService;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.provider.ExternalJWTIdentityProvider;
import io.fusionauth.pem.PEMDecoder;
import io.fusionauth.pem.PEMEncoder;
import io.fusionauth.pem.domain.PEM;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;

public class Migration_1_15_3 implements Migration {
  private final DataSource dataSource;
  
  private final IdentityProviderReaderService identityProviderReader;
  
  private final IdentityProviderService identityProviderService;
  
  private final KeyReaderService keyReader;
  
  private final KeyService keyService;
  
  private final ObjectMapper objectMapper;
  
  @Inject
  public Migration_1_15_3(@Named("primary") DataSource paramDataSource, IdentityProviderReaderService paramIdentityProviderReaderService, IdentityProviderService paramIdentityProviderService, KeyReaderService paramKeyReaderService, KeyService paramKeyService, ObjectMapper paramObjectMapper) {
    this.dataSource = paramDataSource;
    this.identityProviderReader = paramIdentityProviderReaderService;
    this.identityProviderService = paramIdentityProviderService;
    this.keyReader = paramKeyReaderService;
    this.keyService = paramKeyService;
    this.objectMapper = paramObjectMapper;
  }
  
  public void cleanup() {}
  
  public void runOnce() {
    HashMap<Object, Object> hashMap = new HashMap<>();
    try {
      Connection connection = this.dataSource.getConnection();
      try {
        connection.setAutoCommit(false);
        (new Select(connection))
          
          .in("SELECT id, name, data, keys_id FROM identity_providers WHERE type = 'ExternalJWT'")
          .go(paramResultSet -> rowHandler(paramResultSet, paramMap));
        connection.commit();
        if (connection != null)
          connection.close(); 
      } catch (Throwable throwable) {
        if (connection != null)
          try {
            connection.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (SQLException sQLException) {
      throw new RuntimeException(sQLException);
    } 
    for (UUID uUID : hashMap.keySet()) {
      ExternalJWTIdentityProvider externalJWTIdentityProvider = (ExternalJWTIdentityProvider)this.identityProviderReader.retrieveById(null, uUID);
      externalJWTIdentityProvider.defaultKeyId = (UUID)hashMap.get(uUID);
      this.identityProviderService.update(externalJWTIdentityProvider, externalJWTIdentityProvider, null);
    } 
  }
  
  private void rowHandler(ResultSet paramResultSet, Map<UUID, UUID> paramMap) throws Select.SelectException {
    try {
      byte[] arrayOfByte = paramResultSet.getBytes("data");
      Map map = (Map)this.objectMapper.readerFor(Map.class).readValue(arrayOfByte);
      if (map != null && map.containsKey("keys")) {
        Map map1 = (Map)map.get("keys");
        byte b = 1;
        for (String str : map1.keySet()) {
          Key key = str.equals("") ? null : this.keyReader.retrieveByKid(str);
          if (key == null) {
            Key key1 = new Key();
            String str1 = (String)map1.get(str);
            PEM pEM = (new PEMDecoder()).decode(str1);
            if (pEM.certificate != null) {
              key1.certificate = (new PEMEncoder()).encode(pEM.certificate);
            } else {
              key1.publicKey = (new PEMEncoder()).encode(pEM.publicKey);
            } 
            key1.name = "Verification key for " + paramResultSet.getString("name").trim() + " IdP (key " + b + ")";
            this.keyService.create(key1);
            if (str.equals(""))
              paramMap.put(UUIDTools.convert(paramResultSet.getObject("id")), key1.id); 
          } 
          b++;
        } 
      } 
    } catch (Exception exception) {
      throw new Select.SelectException(exception);
    } 
  }
}
