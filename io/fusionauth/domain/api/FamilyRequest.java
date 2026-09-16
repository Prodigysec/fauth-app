package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.FamilyMember;

public class FamilyRequest {
  public FamilyMember familyMember;
  
  @JacksonConstructor
  public FamilyRequest() {}
  
  public FamilyRequest(FamilyMember paramFamilyMember) {
    this.familyMember = paramFamilyMember;
  }
}
