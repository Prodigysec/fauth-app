package io.fusionauth.api.service.cache;

import com.inversoft.cache.SimpleTimestampedSingleValueCache;
import java.net.InetAddress;
import java.util.Map;

public class IpReputationCache extends SimpleTimestampedSingleValueCache<Map<InetAddress, Integer>> {}
