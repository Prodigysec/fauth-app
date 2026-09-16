package io.fusionauth.api.service.cache;

import com.inversoft.cache.SimpleCache;
import io.fusionauth.api.domain.CompiledLambda;
import java.util.UUID;

public class LambdaCache extends SimpleCache<UUID, CompiledLambda> {}
