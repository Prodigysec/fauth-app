package io.fusionauth.api.metrics;

public class HealthChecks {
  public static final String Database = "Database-primary";
  
  public static final String ElasticsearchCluster = "ElasticsearchCluster";
  
  public static final String HikariPoolConnection = "Database-primary.pool.Connection99Percent";
  
  public static final String HikariPoolConnectivity = "Database-primary.pool.ConnectivityCheck";
}
