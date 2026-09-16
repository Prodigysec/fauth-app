package io.fusionauth.domain.search;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.Objects;

public class SortField {
  public String missing = "_last";
  
  public String name;
  
  public Sort order = Sort.asc;
  
  @JacksonConstructor
  public SortField() {}
  
  public SortField(String paramString) {
    this.name = paramString;
  }
  
  public SortField(String paramString, Sort paramSort) {
    this.name = paramString;
    this.order = paramSort;
  }
  
  public SortField(String paramString1, Sort paramSort, String paramString2) {
    this.name = paramString1;
    this.order = paramSort;
    this.missing = paramString2;
  }
  
  public boolean equals(Object paramObject) {
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    SortField sortField = (SortField)paramObject;
    return (Objects.equals(this.missing, sortField.missing) && Objects.equals(this.name, sortField.name) && this.order == sortField.order);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.missing, this.name, this.order });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
