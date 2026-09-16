package io.fusionauth.api.service.comment;

import com.inversoft.error.Errors;
import io.fusionauth.domain.UserComment;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.UserCommentSearchCriteria;
import java.util.List;
import java.util.UUID;

public interface UserCommentService {
  UserComment createComment(UserComment paramUserComment);
  
  List<UserComment> retrieveAllForUser(UUID paramUUID);
  
  SearchResults<UserComment> search(UserCommentSearchCriteria paramUserCommentSearchCriteria);
  
  Errors validate(UUID paramUUID, UserComment paramUserComment);
}
