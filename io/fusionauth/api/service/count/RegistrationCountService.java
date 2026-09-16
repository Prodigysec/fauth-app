package io.fusionauth.api.service.count;

import io.fusionauth.api.domain.IntervalCount;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public interface RegistrationCountService {
  void deleteCountsForApplication(UUID paramUUID);
  
  void incrementApplication(UUID paramUUID, Map<Integer, AtomicInteger> paramMap);
  
  void incrementGlobal(Map<Integer, AtomicInteger> paramMap);
  
  void insertApplication(UUID paramUUID, int paramInt);
  
  void insertApplication(UUID paramUUID, int paramInt1, int paramInt2);
  
  void insertGlobal(int paramInt);
  
  void insertGlobal(int paramInt1, int paramInt2);
  
  List<IntervalCount> retrieveApplicationBetween(UUID paramUUID, int paramInt1, int paramInt2);
  
  long retrieveApplicationCurrentTotal(UUID paramUUID);
  
  long retrieveApplicationTotal(UUID paramUUID);
  
  List<IntervalCount> retrieveGlobalBetween(int paramInt1, int paramInt2);
  
  long retrieveGlobalCurrentTotal();
  
  long retrieveGlobalMonthlyActiveTotal(int paramInt);
  
  long retrieveGlobalTotal();
}
