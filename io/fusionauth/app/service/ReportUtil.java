package io.fusionauth.app.service;

import io.fusionauth.domain.api.report.Count;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class ReportUtil {
  public static ReportData calculate(List<Count> paramList, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2, ZoneId paramZoneId, Function<ZonedDateTime, ZonedDateTime> paramFunction, String paramString, BiFunction<Integer, ZoneId, ZonedDateTime> paramBiFunction) {
    ZonedDateTime zonedDateTime = paramZonedDateTime1;
    paramZonedDateTime1 = paramFunction.apply(paramZonedDateTime1);
    ReportData reportData = new ReportData();
    while (paramZonedDateTime1.isBefore(paramZonedDateTime2) || paramZonedDateTime1.isEqual(paramZonedDateTime2)) {
      reportData.labels.add(zonedDateTime.format(DateTimeFormatter.ofPattern(paramString)));
      ZonedDateTime zonedDateTime1 = zonedDateTime;
      ZonedDateTime zonedDateTime2 = paramZonedDateTime1;
      Predicate predicate = paramCount -> {
          ZonedDateTime zonedDateTime = paramBiFunction.apply(Integer.valueOf(paramCount.interval), paramZoneId);
          return (zonedDateTime.isEqual(paramZonedDateTime1) || (zonedDateTime.isAfter(paramZonedDateTime1) && zonedDateTime.isBefore(paramZonedDateTime2)));
        };
      long l = paramList.stream().filter(predicate).mapToInt(paramCount -> paramCount.count).sum();
      reportData.counts.add(Long.valueOf(l));
      zonedDateTime = paramZonedDateTime1;
      paramZonedDateTime1 = paramFunction.apply(paramZonedDateTime1);
    } 
    return reportData;
  }
  
  public static class ReportData {
    public List<Long> counts = new ArrayList<>();
    
    public List<String> labels = new ArrayList<>();
  }
}
