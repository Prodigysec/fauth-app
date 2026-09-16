package io.fusionauth.api.service.cache;

import com.inversoft.cache.SimpleTimestampedSingleValueCache;
import java.util.List;

public class UserAgentReputationCache extends SimpleTimestampedSingleValueCache<List<String>> {}
