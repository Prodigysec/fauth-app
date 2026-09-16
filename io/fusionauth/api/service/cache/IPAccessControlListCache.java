package io.fusionauth.api.service.cache;

import com.inversoft.cache.SimpleCache;
import com.inversoft.util.StringTools;
import io.fusionauth.api.domain.ip.acl.CompiledIPAccessControlList;
import java.util.UUID;

public class IPAccessControlListCache extends SimpleCache<UUID, CompiledIPAccessControlList> {
  public boolean isBlocked(UUID paramUUID, String paramString) {
    if (paramUUID == null || StringTools.isTrimmedEmpty(paramString))
      return false; 
    CompiledIPAccessControlList compiledIPAccessControlList = (CompiledIPAccessControlList)get(paramUUID);
    return (compiledIPAccessControlList != null && compiledIPAccessControlList.isBlocked(paramString));
  }
}
