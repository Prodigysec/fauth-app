package io.fusionauth.api.service.comment;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.UserCommentMapper;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.domain.UserComment;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.UserCommentSearchCriteria;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DefaultUserCommentService implements UserCommentService {
  private final UserCommentMapper userCommentMapper;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultUserCommentService(UserCommentMapper paramUserCommentMapper, UserReaderService paramUserReaderService) {
    this.userCommentMapper = paramUserCommentMapper;
    this.userReader = paramUserReaderService;
  }
  
  public UserComment createComment(UserComment paramUserComment) {
    paramUserComment.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramUserComment.id = UUID.randomUUID();
    this.userCommentMapper.create(paramUserComment);
    return paramUserComment;
  }
  
  public List<UserComment> retrieveAllForUser(UUID paramUUID) {
    return this.userCommentMapper.retrieveAllForUser(paramUUID);
  }
  
  public SearchResults<UserComment> search(UserCommentSearchCriteria paramUserCommentSearchCriteria) {
    int i = this.userCommentMapper.retrieveCountByCriteria(paramUserCommentSearchCriteria);
    if (i == 0)
      return new SearchResults<>(Collections.emptyList(), i); 
    List<UserComment> list = this.userCommentMapper.retrieveByCriteria(paramUserCommentSearchCriteria);
    return new SearchResults<>(list, i);
  }
  
  public Errors validate(UUID paramUUID, UserComment paramUserComment) {
    return (new Validator())
      .notMissing(paramUserComment.commenterId, "userComment.commenterId", new Object[0])
      .notMissing(paramUserComment.userId, "userComment.userId", new Object[0])
      .notBlank(paramUserComment.comment, "userComment.comment", new Object[0])
      .ifNoErrors(paramValidator -> paramValidator.valid((this.userReader.retrieveById(paramUUID, paramUserComment.commenterId) != null), "userComment.commenterId", new Object[] { paramUserComment.commenterId }).valid((this.userReader.retrieveById(paramUUID, paramUserComment.userId) != null), "userComment.userId", new Object[] { paramUserComment.userId })).done();
  }
}
