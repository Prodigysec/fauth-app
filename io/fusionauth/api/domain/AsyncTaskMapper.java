package io.fusionauth.api.domain;

import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;

public interface AsyncTaskMapper {
  void create(AsyncTask paramAsyncTask);
  
  void delete(AsyncTask paramAsyncTask);
  
  void relinquishTasks(@Param("nodeId") UUID paramUUID);
  
  List<AsyncTask> retrieveAll();
  
  AsyncTask retrieveByEntityId(@Param("entityId") UUID paramUUID);
  
  List<AsyncTask> retrieveByStatus(@Param("status") AsyncTask.AsyncTaskStatus paramAsyncTaskStatus);
  
  void update(AsyncTask paramAsyncTask);
}
