package io.fusionauth.domain.api.report;

import java.util.ArrayList;
import java.util.List;

public class LoginReportResponse {
  public List<Count> hourlyCounts = new ArrayList<>();
  
  public long total;
}
