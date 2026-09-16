package io.fusionauth.api.domain;

import io.fusionauth.domain.Family;
import io.fusionauth.domain.FamilyMember;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface FamilyMapper {
  @Delete({"DELETE FROM families WHERE family_id = #{familyId} AND users_id = #{userId}"})
  int deleteMember(@Param("familyId") UUID paramUUID1, @Param("userId") UUID paramUUID2);
  
  @Delete({"DELETE FROM families WHERE users_id = #{userId}"})
  int removeFromAllFamilies(@Param("userId") UUID paramUUID);
  
  List<Family> retrieveByUserId(UUID paramUUID);
  
  List<FamilyMember> retrieveFamilyById(@Param("familyId") UUID paramUUID);
  
  List<FamilyMember> retrieveMemberByUserId(@Param("userId") UUID paramUUID);
  
  void upsertMember(@Param("familyId") UUID paramUUID, @Param("familyMember") FamilyMember paramFamilyMember);
}
