package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.LoginRecordSearchCriteria;
import java.time.ZoneId;

public class LoginRecordExportRequest extends BaseExportRequest {
  public LoginRecordSearchCriteria criteria;
  
  @JacksonConstructor
  public LoginRecordExportRequest() {}
  
  public LoginRecordExportRequest(LoginRecordSearchCriteria paramLoginRecordSearchCriteria, String paramString, ZoneId paramZoneId) {
    this.criteria = paramLoginRecordSearchCriteria;
    this.dateTimeSecondsFormat = paramString;
    this.zoneId = paramZoneId;
  }
}
