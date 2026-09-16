package io.fusionauth.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import java.net.InetAddress;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public interface FusionAuthNodeMapper {
  void delete(UUID paramUUID);
  
  List<FusionAuthNode> retrieveAll();
  
  FusionAuthNode retrieveById(UUID paramUUID);
  
  void upsert(FusionAuthNode paramFusionAuthNode);
  
  public static class FusionAuthNode implements JSONColumnable {
    public static final UUID ID = UUID.randomUUID();
    
    public Map<String, Object> data = new LinkedHashMap<>();
    
    public UUID id;
    
    public ZonedDateTime insertInstant;
    
    @JSONColumn
    public Map<String, List<InetAddress>> ipAddresses;
    
    public ZonedDateTime lastCheckinInstant;
    
    public boolean me;
    
    @JSONColumn
    public FusionAuthNodeMapper.Platform platform;
    
    public RuntimeMode runtimeMode;
    
    public int uptimeInDays;
    
    public String url;
    
    @JacksonConstructor
    public FusionAuthNode() {}
    
    public FusionAuthNode(UUID param1UUID, ZonedDateTime param1ZonedDateTime, Map<String, List<InetAddress>> param1Map, RuntimeMode param1RuntimeMode, String param1String) {
      this.id = param1UUID;
      this.lastCheckinInstant = param1ZonedDateTime;
      this.ipAddresses = param1Map;
      this.platform = new FusionAuthNodeMapper.Platform();
      this.runtimeMode = param1RuntimeMode;
      this.url = param1String;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      FusionAuthNode fusionAuthNode = (FusionAuthNode)param1Object;
      return (this.me == fusionAuthNode.me && this.uptimeInDays == fusionAuthNode.uptimeInDays && Objects.equals(this.data, fusionAuthNode.data) && Objects.equals(this.id, fusionAuthNode.id) && Objects.equals(this.insertInstant, fusionAuthNode.insertInstant) && Objects.equals(this.ipAddresses, fusionAuthNode.ipAddresses) && Objects.equals(this.lastCheckinInstant, fusionAuthNode.lastCheckinInstant) && Objects.equals(this.platform, fusionAuthNode.platform) && this.runtimeMode == fusionAuthNode.runtimeMode && Objects.equals(this.url, fusionAuthNode.url));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.data, this.id, this.insertInstant, this.ipAddresses, this.lastCheckinInstant, Boolean.valueOf(this.me), this.platform, this.runtimeMode, Integer.valueOf(this.uptimeInDays), this.url });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
  
  public static class Platform {
    public String arch = System.getProperty("os.arch");
    
    public String name = System.getProperty("os.name");
    
    public String version = System.getProperty("os.version");
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      Platform platform = (Platform)param1Object;
      return (Objects.equals(this.arch, platform.arch) && Objects.equals(this.name, platform.name) && Objects.equals(this.version, platform.version));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.arch, this.name, this.version });
    }
    
    @JsonIgnore
    public String toDisplayString() {
      if (this.name == null)
        return null; 
      return this.name + this.name + (
        (this.version != null) ? (" " + this.version) : "");
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
}
