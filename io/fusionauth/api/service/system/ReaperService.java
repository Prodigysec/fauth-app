package io.fusionauth.api.service.system;

public interface ReaperService {
  int reapAuditLogs();
  
  int reapLoginRecords();
  
  int reapWebhookEventLogs();
  
  int reapUsageStats();
}
