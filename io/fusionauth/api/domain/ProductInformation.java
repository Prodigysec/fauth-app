package io.fusionauth.api.domain;

import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import java.util.List;
import java.util.Objects;

public class ProductInformation implements Buildable<ProductInformation> {
  public String currentProductVersion;
  
  public String dbEngine;
  
  public String dbEngineVersion;
  
  public String latestProductVersion;
  
  public List<FusionAuthNodeMapper.FusionAuthNode> nodes;
  
  public boolean productUpdateAvailable;
  
  public RuntimeMode runtimeMode;
  
  public String searchEngine;
  
  public String searchEngineDistribution;
  
  public String searchEngineVersion;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    ProductInformation productInformation = (ProductInformation)paramObject;
    return (this.productUpdateAvailable == productInformation.productUpdateAvailable && 
      Objects.equals(this.currentProductVersion, productInformation.currentProductVersion) && 
      Objects.equals(this.dbEngine, productInformation.dbEngine) && 
      Objects.equals(this.dbEngineVersion, productInformation.dbEngineVersion) && 
      Objects.equals(this.latestProductVersion, productInformation.latestProductVersion) && 
      Objects.equals(this.nodes, productInformation.nodes) && this.runtimeMode == productInformation.runtimeMode && 
      
      Objects.equals(this.searchEngine, productInformation.searchEngine) && 
      Objects.equals(this.searchEngineDistribution, productInformation.searchEngineDistribution) && 
      Objects.equals(this.searchEngineVersion, productInformation.searchEngineVersion));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.currentProductVersion, this.dbEngine, this.dbEngineVersion, this.latestProductVersion, this.nodes, 



          
          Boolean.valueOf(this.productUpdateAvailable), this.runtimeMode, this.searchEngine, this.searchEngineDistribution, this.searchEngineVersion });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
