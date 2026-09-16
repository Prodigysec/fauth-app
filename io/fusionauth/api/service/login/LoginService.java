package io.fusionauth.api.service.login;

import io.fusionauth.domain.DisplayableRawLogin;
import io.fusionauth.domain.search.LoginRecordSearchCriteria;
import java.io.OutputStream;
import java.time.ZoneId;
import java.util.List;

public interface LoginService {
  void exportLogins(OutputStream paramOutputStream, LoginRecordSearchCriteria paramLoginRecordSearchCriteria, String paramString, ZoneId paramZoneId);
  
  List<DisplayableRawLogin> retrieveRawLoginsByCriteria(LoginRecordSearchCriteria paramLoginRecordSearchCriteria);
  
  int retrieveRawLoginsByCriteriaCount(LoginRecordSearchCriteria paramLoginRecordSearchCriteria);
}
