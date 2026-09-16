package io.fusionauth.domain.html;

import io.fusionauth.domain.Buildable;
import java.net.URI;
import java.util.Objects;

public class Favicon implements Buildable<Favicon> {
  public URI href;
  
  public String rel = "icon";
  
  public String sizes;
  
  public String type = "image/x-icon";
  
  public Favicon() {}
  
  public Favicon(Favicon paramFavicon) {
    this.href = paramFavicon.href;
    this.rel = paramFavicon.rel;
    this.sizes = paramFavicon.sizes;
    this.type = paramFavicon.type;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    Favicon favicon = (Favicon)paramObject;
    return (Objects.equals(this.href, favicon.href) && 
      Objects.equals(this.rel, favicon.rel) && 
      Objects.equals(this.sizes, favicon.sizes) && 
      Objects.equals(this.type, favicon.type));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.href, this.rel, this.sizes, this.type });
  }
}
