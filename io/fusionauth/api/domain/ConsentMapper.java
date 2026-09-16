package io.fusionauth.api.domain;

import io.fusionauth.domain.Consent;
import io.fusionauth.domain.UserConsent;
import io.fusionauth.domain.search.ConsentSearchCriteria;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface ConsentMapper {
  void createConsent(Consent paramConsent);
  
  @Insert({"INSERT INTO user_consents_email_plus (next_email_instant, user_consents_id) VALUES(#{nextEmailInstant}, #{userConsentId})"})
  void createEmailPlus(@Param("nextEmailInstant") long paramLong, @Param("userConsentId") UUID paramUUID);
  
  void createUserConsent(UserConsent paramUserConsent);
  
  int deleteConsent(UUID paramUUID);
  
  @Delete({"DELETE FROM user_consents WHERE users_id = #{userId} OR giver_users_id = #{userId}"})
  int deleteConsentsByUserId(UUID paramUUID);
  
  @Delete({"DELETE FROM user_consents_email_plus WHERE id = #{id}"})
  int deleteEmailPlus(int paramInt);
  
  @Delete({"DELETE from user_consents_email_plus WHERE user_consents_id = #{consentId}"})
  int deleteEmailPlusConsentByUserConsentId(UUID paramUUID);
  
  int deleteUserConsentsByConsentId(UUID paramUUID);
  
  List<Consent> retrieveAllConsents();
  
  @ResultMap({"EmailPlusConsent"})
  @Select({"SELECT id, next_email_instant, user_consents_id FROM user_consents_email_plus"})
  List<EmailPlusConsent> retrieveAllEmailPlus();
  
  List<UserConsent> retrieveAllUserConsentsOrGivenByUserId(UUID paramUUID);
  
  Consent retrieveConsentById(UUID paramUUID);
  
  Consent retrieveConsentByName(String paramString);
  
  int retrieveConsentCountByCriteria(ConsentSearchCriteria paramConsentSearchCriteria);
  
  List<Consent> retrieveConsentsByCriteria(ConsentSearchCriteria paramConsentSearchCriteria);
  
  @ResultMap({"EmailPlusConsent"})
  @Select({"SELECT id, next_email_instant, user_consents_id FROM user_consents_email_plus WHERE next_email_instant < #{now} LIMIT #{limit}"})
  List<EmailPlusConsent> retrieveEmailPlusPastCutoff(@Param("now") ZonedDateTime paramZonedDateTime, @Param("limit") int paramInt);
  
  Consent retrieveExistingConsent(@Param("id") UUID paramUUID, @Param("name") String paramString);
  
  UserConsent retrieveUserConsentById(UUID paramUUID);
  
  List<UserConsent> retrieveUserConsentByUserId(UUID paramUUID);
  
  void updateConsent(Consent paramConsent);
  
  void updateUserConsent(UserConsent paramUserConsent);
  
  public static class EmailPlusConsent {
    public int id;
    
    public ZonedDateTime nextEmailInstant;
    
    public UUID userConsentId;
  }
}
