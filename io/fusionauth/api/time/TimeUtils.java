package io.fusionauth.api.time;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
  public static ZonedDateTime fromDays(int paramInt, ZoneId paramZoneId) {
    long l = paramInt * 3600000L * 24L;
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneOffset.UTC).withZoneSameLocal(paramZoneId);
  }
  
  public static ZonedDateTime fromHours(long paramLong, ZoneId paramZoneId) {
    long l = paramLong * 3600000L;
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneOffset.UTC).withZoneSameInstant(paramZoneId);
  }
  
  public static ZonedDateTime fromMonths(int paramInt, ZoneId paramZoneId) {
    int i = paramInt / 12 + 1970;
    paramInt = paramInt % 12 + 1;
    return ZonedDateTime.of(i, paramInt, 1, 0, 0, 0, 0, paramZoneId);
  }
  
  public static void main(String[] paramArrayOfString) {
    char c;
    for (c = 'Ƞ'; c < 'Ȧ'; c++)
      System.out.println("month " + c + ": " + String.valueOf(fromMonths(c, ZoneId.of("America/Denver")))); 
    for (c = '䄊'; c < '䅐'; c++)
      System.out.println("day " + c + ": " + String.valueOf(fromDays(c, ZoneId.of("America/Denver")))); 
  }
  
  public static int toDay(ZonedDateTime paramZonedDateTime) {
    long l = paramZonedDateTime.toInstant().toEpochMilli() + paramZonedDateTime.getOffset().getTotalSeconds() * 1000L;
    return (int)(l / TimeUnit.DAYS.toMillis(1L));
  }
  
  public static int toHour(ZonedDateTime paramZonedDateTime) {
    long l = paramZonedDateTime.toInstant().toEpochMilli();
    return (int)(l / TimeUnit.HOURS.toMillis(1L));
  }
  
  public static int toMonth(ZonedDateTime paramZonedDateTime) {
    int i = paramZonedDateTime.getYear() - 1970;
    int j = paramZonedDateTime.getMonthValue() - 1;
    return i * 12 + j;
  }
}
