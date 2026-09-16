package io.fusionauth.app.service.system;

import com.inversoft.util.Pair;
import io.fusionauth.api.domain.FusionAuthNodeMapper;
import io.fusionauth.http.server.HTTPResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface SystemLogFrontendService {
  void downloadLogs(String paramString1, HTTPResponse paramHTTPResponse, String paramString2, ZoneId paramZoneId, boolean paramBoolean) throws IOException;
  
  SystemLogResult retrieveLogsByNodeId(UUID paramUUID, int paramInt) throws IOException;
  
  public static class SystemLogResult {
    public List<Pair<String, String>> logs = new ArrayList<>();
    
    public FusionAuthNodeMapper.FusionAuthNode node;
    
    public int nodeCount;
  }
}
