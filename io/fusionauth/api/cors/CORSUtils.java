package io.fusionauth.api.cors;

import io.fusionauth.domain.CORSConfiguration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class CORSUtils {
  public static CORSConfiguration mergeConfiguration(CORSConfiguration paramCORSConfiguration, Collection<CORSConfiguration> paramCollection) {
    CORSConfiguration cORSConfiguration = new CORSConfiguration();
    cORSConfiguration.enabled = (paramCORSConfiguration.enabled || paramCollection.size() > 0);
    cORSConfiguration.allowCredentials = paramCORSConfiguration.allowCredentials;
    cORSConfiguration.allowedHeaders.addAll(merge(paramCORSConfiguration.allowedHeaders, paramCollection, paramCORSConfiguration -> paramCORSConfiguration.allowedHeaders));
    cORSConfiguration.allowedMethods.addAll(merge(paramCORSConfiguration.allowedMethods, paramCollection, paramCORSConfiguration -> paramCORSConfiguration.allowedMethods));
    cORSConfiguration.allowedOrigins.addAll(merge(paramCORSConfiguration.allowedOrigins, paramCollection, paramCORSConfiguration -> paramCORSConfiguration.allowedOrigins));
    cORSConfiguration.exposedHeaders.addAll(merge(paramCORSConfiguration.exposedHeaders, paramCollection, paramCORSConfiguration -> paramCORSConfiguration.exposedHeaders));
    cORSConfiguration.preflightMaxAgeInSeconds = paramCORSConfiguration.preflightMaxAgeInSeconds;
    return cORSConfiguration;
  }
  
  private static <T> List<T> merge(List<T> paramList, Collection<CORSConfiguration> paramCollection, Function<CORSConfiguration, List<T>> paramFunction) {
    if (paramCollection.size() == 0)
      return paramList; 
    ArrayList<T> arrayList = new ArrayList<>(paramList);
    for (CORSConfiguration cORSConfiguration : paramCollection) {
      List list = paramFunction.apply(cORSConfiguration);
      for (T t : list) {
        if (!arrayList.contains(t))
          arrayList.add(t); 
      } 
    } 
    return arrayList;
  }
}
