package io.fusionauth.api.service.useraction;

import com.google.inject.Inject;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.UserActionReasonMapper;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.domain.UserActionReason;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class DefaultUserActionReasonService implements UserActionReasonService {
  private final UserActionReasonMapper userActionReasonMapper;
  
  @Inject
  public DefaultUserActionReasonService(UserActionReasonMapper paramUserActionReasonMapper) {
    this.userActionReasonMapper = paramUserActionReasonMapper;
  }
  
  public void create(UserActionReason paramUserActionReason) {
    if (paramUserActionReason.id == null)
      paramUserActionReason.id = UUID.randomUUID(); 
    paramUserActionReason.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramUserActionReason.lastUpdateInstant = paramUserActionReason.insertInstant;
    this.userActionReasonMapper.create(paramUserActionReason);
  }
  
  public boolean delete(UUID paramUUID) {
    return (this.userActionReasonMapper.delete(paramUUID) >= 1);
  }
  
  public List<UserActionReason> retrieveAll() {
    return this.userActionReasonMapper.retrieveAll();
  }
  
  public UserActionReason retrieveByCode(String paramString) {
    return this.userActionReasonMapper.retrieveByCode(paramString);
  }
  
  public UserActionReason retrieveById(UUID paramUUID) {
    return this.userActionReasonMapper.retrieveById(paramUUID);
  }
  
  public UserActionReason retrieveByText(String paramString) {
    return this.userActionReasonMapper.retrieveByText(paramString);
  }
  
  public void update(UserActionReason paramUserActionReason1, UserActionReason paramUserActionReason2) {
    paramUserActionReason2.insertInstant = paramUserActionReason1.insertInstant;
    paramUserActionReason2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.userActionReasonMapper.update(paramUserActionReason2);
  }
  
  public UserActionReasonService.ValidationResult validate(UserActionReason paramUserActionReason, boolean paramBoolean) {
    UserActionReasonService.ValidationResult validationResult = new UserActionReasonService.ValidationResult();
    Validator validator = new Validator();
    validator.notBlank(paramUserActionReason.code, "userActionReason.code", new Object[0])
      .notBlank(paramUserActionReason.text, "userActionReason.text", new Object[0])
      .ifTrue((paramUserActionReason.localizedTexts != null), paramValidator -> paramUserActionReason.localizedTexts.forEach(()))



      
      .maxLength(paramUserActionReason.code, MapperTools.MaximumIndexedColumnLength, "userActionReason.code", new Object[] { Integer.valueOf(MapperTools.MaximumIndexedColumnLength) }).maxLength(paramUserActionReason.text, MapperTools.MaximumIndexedColumnLength, "userActionReason.text", new Object[] { Integer.valueOf(MapperTools.MaximumIndexedColumnLength) });
    if (paramBoolean) {
      UserActionReason userActionReason1 = (paramUserActionReason.id != null) ? this.userActionReasonMapper.retrieveById(paramUserActionReason.id) : null;
      UserActionReason userActionReason2 = (paramUserActionReason.text != null) ? this.userActionReasonMapper.retrieveByText(paramUserActionReason.text) : null;
      UserActionReason userActionReason3 = (paramUserActionReason.code != null) ? this.userActionReasonMapper.retrieveByCode(paramUserActionReason.code) : null;
      validator.notDuplicate(userActionReason1, "userActionReasonId", new Object[] { paramUserActionReason.id }).notDuplicate(userActionReason3, "userActionReason.code", new Object[] { paramUserActionReason.code }).notDuplicate(userActionReason2, "userActionReason.text", new Object[] { paramUserActionReason.text });
    } else {
      validator.notMissing(paramUserActionReason.id, "userActionReasonId", new Object[0])
        .ifTrue((paramUserActionReason.text != null), paramValidator -> {
            Objects.requireNonNull(this.userActionReasonMapper);
            paramValidator.notDuplicate(paramUserActionReason, this.userActionReasonMapper::retrieveExistingByText, "userActionReason.text", new Object[] { paramUserActionReason.text });
          }).ifTrue((paramUserActionReason.code != null), paramValidator -> {
            Objects.requireNonNull(this.userActionReasonMapper);
            paramValidator.notDuplicate(paramUserActionReason, this.userActionReasonMapper::retrieveExistingByCode, "userActionReason.code", new Object[] { paramUserActionReason.code });
          });
    } 
    validationResult.existing = paramBoolean ? null : this.userActionReasonMapper.retrieveById(paramUserActionReason.id);
    validationResult.errors = validator.done();
    return validationResult;
  }
}
