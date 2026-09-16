package io.fusionauth.api.service.moderation.cleanspeak;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class FilterRule {
  public static final Comparator<Locale> localeComparator;
  
  public FilterAction highAction;
  
  public AlertType highAlertType;
  
  public Integer highUserScoreAdjustment;
  
  static {
    localeComparator = ((paramLocale1, paramLocale2) -> paramLocale1.toString().compareTo(paramLocale2.toString()));
  }
  
  public List<Locale> locales = new ArrayList<>();
  
  public FilterAction mediumAction;
  
  public AlertType mediumAlertType;
  
  public Integer mediumUserScoreAdjustment;
  
  public FilterAction mildAction;
  
  public AlertType mildAlertType;
  
  public Integer mildUserScoreAdjustment;
  
  public FilterAction severeAction;
  
  public AlertType severeAlertType;
  
  public Integer severeUserScoreAdjustment;
  
  public List<String> tags = new ArrayList<>();
  
  public FilterRule(List<String> paramList, List<Locale> paramList1, FilterAction paramFilterAction1, AlertType paramAlertType1, Integer paramInteger1, FilterAction paramFilterAction2, AlertType paramAlertType2, Integer paramInteger2, FilterAction paramFilterAction3, AlertType paramAlertType3, Integer paramInteger3, FilterAction paramFilterAction4, AlertType paramAlertType4, Integer paramInteger4) {
    this.tags = paramList;
    this.highAction = paramFilterAction1;
    this.highAlertType = paramAlertType1;
    this.highUserScoreAdjustment = paramInteger1;
    this.locales = paramList1;
    this.mediumAction = paramFilterAction2;
    this.mediumAlertType = paramAlertType2;
    this.mediumUserScoreAdjustment = paramInteger2;
    this.mildAction = paramFilterAction3;
    this.mildAlertType = paramAlertType3;
    this.mildUserScoreAdjustment = paramInteger3;
    this.severeAction = paramFilterAction4;
    this.severeAlertType = paramAlertType4;
    this.severeUserScoreAdjustment = paramInteger4;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  @JacksonConstructor
  public FilterRule() {}
}
