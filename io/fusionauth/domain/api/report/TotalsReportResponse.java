package io.fusionauth.domain.api.report;

import com.inversoft.json.JacksonConstructor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TotalsReportResponse {
  public Map<UUID, Totals> applicationTotals = new HashMap<>();
  
  public long globalRegistrations;
  
  public long totalGlobalRegistrations;
  
  public static class Totals {
    public long logins;
    
    public long registrations;
    
    public long totalRegistrations;
    
    @JacksonConstructor
    public Totals() {}
    
    public Totals(long param1Long1, long param1Long2, long param1Long3) {
      this.logins = param1Long1;
      this.registrations = param1Long2;
      this.totalRegistrations = param1Long3;
    }
  }
}
