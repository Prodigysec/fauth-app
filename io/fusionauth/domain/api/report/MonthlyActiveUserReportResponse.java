package io.fusionauth.domain.api.report;

import java.util.ArrayList;
import java.util.List;

public class MonthlyActiveUserReportResponse {
  public List<Count> monthlyActiveUsers = new ArrayList<>();
  
  public long total;
}
