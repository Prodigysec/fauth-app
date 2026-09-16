package io.fusionauth.api.service.system;

import java.io.OutputStream;

public interface SystemLogService {
  void downloadAllLogs(String paramString, OutputStream paramOutputStream, int paramInt, boolean paramBoolean);
}
