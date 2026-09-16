package io.fusionauth.domain.api.report;

import java.util.ArrayList;
import java.util.List;

public class DailyActiveUserReportResponse {
  public List<Count> dailyActiveUsers = new ArrayList<>();
  
  public long total;
}
