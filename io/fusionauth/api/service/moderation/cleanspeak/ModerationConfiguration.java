package io.fusionauth.api.service.moderation.cleanspeak;

import com.inversoft.json.ToString;
import java.util.ArrayList;
import java.util.List;

public class ModerationConfiguration {
  public Integer approvalCheckOutMinutes = Integer.valueOf(10);
  
  public Integer approvalQueueSize = Integer.valueOf(30);
  
  public Integer contentAlertCheckOutMinutes;
  
  public Integer contentAlertQueueSize;
  
  public boolean contentDeletable;
  
  public boolean contentEditable;
  
  public AlertType contentFlagAlertType;
  
  public Integer contentFlagUserScoreAdjustment;
  
  public boolean contentUserActionsEnabled;
  
  public boolean defaultActionIsQueueForApproval;
  
  public Integer emailFilterMaxLength;
  
  public Double emailFilterSpacePenalty;
  
  public boolean emailOnAlerts;
  
  public boolean emailOnContentFlagged;
  
  public boolean emailOnUserFlagged;
  
  public List<QualityFilterRule> emailRules = new ArrayList<>();
  
  public List<FilterRule> filterRules = new ArrayList<>();
  
  public Integer noRuleUserScoreAdjustment;
  
  public boolean persistent;
  
  public Integer phoneNumberFilterMaxLength;
  
  public Integer phoneNumberFilterMinLength;
  
  public Double phoneNumberFilterSeparatorPenalty;
  
  public Double phoneNumberFilterSpacePenalty;
  
  public Double phoneNumberFilterWordPenalty;
  
  public List<QualityFilterRule> phoneNumberRules = new ArrayList<>();
  
  public boolean queuePersistentContent;
  
  public Character replacementCharacter;
  
  public String replacementString;
  
  public boolean storeContent;
  
  public Integer urlFilterMaxLength;
  
  public Double urlFilterSpacePenalty;
  
  public List<QualityFilterRule> urlRules = new ArrayList<>();
  
  public Integer userCheckOutMinutes = Integer.valueOf(10);
  
  public Integer userFlagUserScoreAdjustment;
  
  public ModerationConfiguration() {}
  
  public ModerationConfiguration(int paramInt1, int paramInt2, boolean paramBoolean1, boolean paramBoolean2, Integer paramInteger1, boolean paramBoolean3, boolean paramBoolean4, Integer paramInteger2, Double paramDouble1, boolean paramBoolean5, boolean paramBoolean6, boolean paramBoolean7, Integer paramInteger3, boolean paramBoolean8, Integer paramInteger4, Integer paramInteger5, Double paramDouble2, Double paramDouble3, Double paramDouble4, Character paramCharacter, String paramString, boolean paramBoolean9, Integer paramInteger6, Integer paramInteger7, Double paramDouble5, Integer paramInteger8, List<QualityFilterRule> paramList1, List<QualityFilterRule> paramList2, List<QualityFilterRule> paramList3, List<FilterRule> paramList) {
    this.approvalQueueSize = Integer.valueOf(paramInt1);
    this.approvalCheckOutMinutes = Integer.valueOf(paramInt2);
    this.contentDeletable = paramBoolean1;
    this.contentEditable = paramBoolean2;
    this.contentFlagUserScoreAdjustment = paramInteger1;
    this.contentUserActionsEnabled = paramBoolean3;
    this.defaultActionIsQueueForApproval = paramBoolean4;
    this.emailOnAlerts = paramBoolean5;
    this.emailOnContentFlagged = paramBoolean6;
    this.emailOnUserFlagged = paramBoolean7;
    this.emailFilterMaxLength = paramInteger2;
    this.emailFilterSpacePenalty = paramDouble1;
    this.emailRules.addAll(paramList1);
    this.filterRules.addAll(paramList);
    this.noRuleUserScoreAdjustment = paramInteger3;
    this.persistent = paramBoolean8;
    this.phoneNumberFilterMaxLength = paramInteger4;
    this.phoneNumberFilterMinLength = paramInteger5;
    this.phoneNumberFilterSeparatorPenalty = paramDouble2;
    this.phoneNumberFilterSpacePenalty = paramDouble3;
    this.phoneNumberFilterWordPenalty = paramDouble4;
    this.phoneNumberRules.addAll(paramList2);
    this.replacementCharacter = paramCharacter;
    this.replacementString = paramString;
    this.storeContent = paramBoolean9;
    this.userCheckOutMinutes = paramInteger6;
    this.urlFilterMaxLength = paramInteger7;
    this.urlFilterSpacePenalty = paramDouble5;
    this.urlRules.addAll(paramList3);
    this.userFlagUserScoreAdjustment = paramInteger8;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
