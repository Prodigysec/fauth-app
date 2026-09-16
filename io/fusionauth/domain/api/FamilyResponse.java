package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Family;
import java.util.List;

public class FamilyResponse {
  public List<Family> families;
  
  public Family family;
  
  @JacksonConstructor
  public FamilyResponse() {}
  
  public FamilyResponse(List<Family> paramList) {
    this.families = paramList;
  }
  
  public FamilyResponse(Family paramFamily) {
    this.family = paramFamily;
  }
}
