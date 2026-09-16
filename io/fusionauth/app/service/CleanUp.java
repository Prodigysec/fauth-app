package io.fusionauth.app.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanUp implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(CleanUp.class);
  
  private final List<DataSource> dataSources;
  
  @Inject
  public CleanUp(@Named("primary") DataSource paramDataSource1, @Named("secondary") DataSource paramDataSource2, @Named("background") DataSource paramDataSource3) {
    this.dataSources = List.of(paramDataSource1, paramDataSource2, paramDataSource3);
  }
  
  public void close() throws IOException {
    logger.info("De-registering JDBC Drivers");
    for (Driver driver : Collections.<Driver>list(DriverManager.getDrivers())) {
      try {
        DriverManager.deregisterDriver(driver);
      } catch (SQLException sQLException) {}
    } 
    logger.info("Unloading Hikari");
    try {
      for (DataSource dataSource : this.dataSources)
        ((Closeable)dataSource).close(); 
    } catch (Exception exception) {}
  }
}
