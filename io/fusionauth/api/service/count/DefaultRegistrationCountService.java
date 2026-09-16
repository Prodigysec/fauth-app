package io.fusionauth.api.service.count;

import com.google.inject.Inject;
import io.fusionauth.api.domain.IntervalCount;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.domain.RegistrationCountMapper;
import io.fusionauth.api.time.TimeUtils;
import io.fusionauth.api.util.MapperTools;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultRegistrationCountService implements RegistrationCountService {
  private final LoginMapper loginMapper;
  
  private final RegistrationCountMapper registrationCountMapper;
  
  @Inject
  public DefaultRegistrationCountService(LoginMapper paramLoginMapper, RegistrationCountMapper paramRegistrationCountMapper) {
    this.loginMapper = paramLoginMapper;
    this.registrationCountMapper = paramRegistrationCountMapper;
  }
  
  public void deleteCountsForApplication(UUID paramUUID) {
    this.registrationCountMapper.deleteRawCountsForApplication(paramUUID);
    this.registrationCountMapper.deleteCountsForApplication(paramUUID);
  }
  
  public void incrementApplication(UUID paramUUID, Map<Integer, AtomicInteger> paramMap) {
    ArrayList<IntervalCount> arrayList = new ArrayList();
    for (Integer integer : paramMap.keySet())
      arrayList.add(new IntervalCount(paramUUID, ((AtomicInteger)paramMap.get(integer)).get(), 0, integer.intValue())); 
    Objects.requireNonNull(this.registrationCountMapper);
    MapperTools.safeCreateUpdate(5000, arrayList, this.registrationCountMapper::bulkUpsertApplication);
  }
  
  public void incrementGlobal(Map<Integer, AtomicInteger> paramMap) {
    ArrayList<IntervalCount> arrayList = new ArrayList();
    for (Integer integer : paramMap.keySet())
      arrayList.add(new IntervalCount(null, ((AtomicInteger)paramMap.get(integer)).get(), 0, integer.intValue())); 
    Objects.requireNonNull(this.registrationCountMapper);
    MapperTools.safeCreateUpdate(5000, arrayList, this.registrationCountMapper::bulkUpsertGlobal);
  }
  
  public void insertApplication(UUID paramUUID, int paramInt) {
    insertApplication(paramUUID, paramInt, TimeUtils.toHour(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS)));
  }
  
  public void insertApplication(UUID paramUUID, int paramInt1, int paramInt2) {
    IntervalCount intervalCount;
    if (paramInt1 >= 0) {
      intervalCount = new IntervalCount(paramUUID, paramInt1, 0, paramInt2);
    } else {
      intervalCount = new IntervalCount(paramUUID, 0, Math.abs(paramInt1), paramInt2);
    } 
    this.registrationCountMapper.insertRawApplication(intervalCount);
  }
  
  public void insertGlobal(int paramInt1, int paramInt2) {
    IntervalCount intervalCount;
    if (paramInt1 >= 0) {
      intervalCount = new IntervalCount(null, paramInt1, 0, paramInt2);
    } else {
      intervalCount = new IntervalCount(null, 0, Math.abs(paramInt1), paramInt2);
    } 
    this.registrationCountMapper.insertRawGlobal(intervalCount);
  }
  
  public void insertGlobal(int paramInt) {
    insertGlobal(paramInt, TimeUtils.toHour(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS)));
  }
  
  public List<IntervalCount> retrieveApplicationBetween(UUID paramUUID, int paramInt1, int paramInt2) {
    return this.registrationCountMapper.retrieveApplicationBetween(paramUUID, paramInt1, paramInt2);
  }
  
  public long retrieveApplicationCurrentTotal(UUID paramUUID) {
    return this.registrationCountMapper.retrieveApplicationCurrentTotal(paramUUID);
  }
  
  public long retrieveApplicationTotal(UUID paramUUID) {
    return this.registrationCountMapper.retrieveApplicationTotal(paramUUID);
  }
  
  public List<IntervalCount> retrieveGlobalBetween(int paramInt1, int paramInt2) {
    return this.registrationCountMapper.retrieveGlobalBetween(paramInt1, paramInt2);
  }
  
  public long retrieveGlobalCurrentTotal() {
    return this.registrationCountMapper.retrieveGlobalCurrentTotal();
  }
  
  public long retrieveGlobalMonthlyActiveTotal(int paramInt) {
    List<IntervalCount> list = this.loginMapper.retrieveGlobalMonthlyActives(paramInt, paramInt + 1);
    return list.stream()
      .mapToLong(paramIntervalCount -> paramIntervalCount.count)
      .sum();
  }
  
  public long retrieveGlobalTotal() {
    return this.registrationCountMapper.retrieveGlobalTotal();
  }
}
