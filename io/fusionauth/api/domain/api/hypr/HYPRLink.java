package io.fusionauth.api.domain.api.hypr;

import com.inversoft.json.ToString;
import java.util.Objects;

public class HYPRLink {
  public String href;
  
  public String rel;
  
  public boolean equals(Object paramObject) {
    HYPRLink hYPRLink;
    if (this == paramObject)
      return true; 
    if (paramObject instanceof HYPRLink) {
      hYPRLink = (HYPRLink)paramObject;
    } else {
      return false;
    } 
    return (Objects.equals(this.href, hYPRLink.href) && 
      Objects.equals(this.rel, hYPRLink.rel));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.href, this.rel });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
