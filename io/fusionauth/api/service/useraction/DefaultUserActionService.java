package io.fusionauth.api.service.useraction;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.EmailTemplateMapper;
import io.fusionauth.api.domain.UserActionLogMapper;
import io.fusionauth.api.domain.UserActionMapper;
import io.fusionauth.domain.TransactionType;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.UserActionOption;
import io.fusionauth.domain.email.EmailTemplate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.mybatis.guice.transactional.Transactional;

public class DefaultUserActionService implements UserActionService {
  private final EmailTemplateMapper emailTemplateMapper;
  
  private final UserActionLogMapper userActionLogMapper;
  
  private final UserActionMapper userActionMapper;
  
  @Inject
  public DefaultUserActionService(EmailTemplateMapper paramEmailTemplateMapper, UserActionLogMapper paramUserActionLogMapper, UserActionMapper paramUserActionMapper) {
    this.emailTemplateMapper = paramEmailTemplateMapper;
    this.userActionLogMapper = paramUserActionLogMapper;
    this.userActionMapper = paramUserActionMapper;
  }
  
  @Transactional
  public void create(UserAction paramUserAction) {
    paramUserAction.active = true;
    if (paramUserAction.transactionType == null)
      paramUserAction.transactionType = TransactionType.None; 
    if (!paramUserAction.temporal) {
      paramUserAction.sendEndEvent = false;
      paramUserAction.modifyEmailTemplateId = null;
      paramUserAction.cancelEmailTemplateId = null;
      paramUserAction.endEmailTemplateId = null;
    } 
    if (paramUserAction.id == null)
      paramUserAction.id = UUID.randomUUID(); 
    paramUserAction.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramUserAction.lastUpdateInstant = paramUserAction.insertInstant;
    this.userActionMapper.create(paramUserAction);
  }
  
  public boolean deactivate(UUID paramUUID) {
    return (this.userActionMapper.deactivate(paramUUID) >= 1);
  }
  
  @Transactional
  public boolean delete(UUID paramUUID) {
    this.userActionLogMapper.deleteLogsForAction(paramUUID);
    return (this.userActionMapper.delete(paramUUID) >= 1);
  }
  
  public UserAction reactivate(UUID paramUUID) {
    UserAction userAction = this.userActionMapper.retrieveByIdIgnoreActive(paramUUID);
    if (userAction == null)
      return null; 
    if (userAction.active)
      return userAction; 
    userAction.active = true;
    this.userActionMapper.update(userAction);
    return userAction;
  }
  
  public List<UserAction> retrieveAll() {
    return this.userActionMapper.retrieveAll();
  }
  
  public List<UserAction> retrieveAllCurrentForUser(UUID paramUUID) {
    return this.userActionMapper.retrieveAllCurrentForUser(paramUUID, ZonedDateTime.now(ZoneOffset.UTC));
  }
  
  public List<UserActionLog> retrieveAllCurrentPreventLoginActionLogsForUser(UUID paramUUID) {
    return this.userActionLogMapper.retrieveAllCurrentPreventLoginActionLogsForUser(paramUUID, ZonedDateTime.now(ZoneOffset.UTC));
  }
  
  public List<UserAction> retrieveAllInactive() {
    return this.userActionMapper.retrieveAllInactive();
  }
  
  public UserAction retrieveById(UUID paramUUID) {
    return this.userActionMapper.retrieveById(paramUUID);
  }
  
  public UserAction retrieveByIdIgnoreActive(UUID paramUUID) {
    return this.userActionMapper.retrieveByIdIgnoreActive(paramUUID);
  }
  
  public UserAction retrieveByName(String paramString) {
    return this.userActionMapper.retrieveByName(paramString);
  }
  
  public UserAction retrieveByNameIgnoreInactive(String paramString) {
    return this.userActionMapper.retrieveByNameIgnoreActive(paramString);
  }
  
  @Transactional
  public boolean update(UserAction paramUserAction) {
    UserAction userAction = this.userActionMapper.retrieveById(paramUserAction.id);
    if (userAction == null)
      return false; 
    if (paramUserAction.transactionType == null)
      paramUserAction.transactionType = userAction.transactionType; 
    paramUserAction.active = true;
    if (!paramUserAction.temporal) {
      paramUserAction.modifyEmailTemplateId = null;
      paramUserAction.cancelEmailTemplateId = null;
      paramUserAction.endEmailTemplateId = null;
    } 
    paramUserAction.insertInstant = userAction.insertInstant;
    paramUserAction.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    return (this.userActionMapper.update(paramUserAction) >= 1);
  }
  
  public Errors validate(UserAction paramUserAction, boolean paramBoolean) {
    EmailTemplate emailTemplate1 = (paramUserAction.startEmailTemplateId != null) ? this.emailTemplateMapper.retrieveById(paramUserAction.startEmailTemplateId) : null;
    EmailTemplate emailTemplate2 = (paramUserAction.modifyEmailTemplateId != null) ? this.emailTemplateMapper.retrieveById(paramUserAction.modifyEmailTemplateId) : null;
    EmailTemplate emailTemplate3 = (paramUserAction.cancelEmailTemplateId != null) ? this.emailTemplateMapper.retrieveById(paramUserAction.cancelEmailTemplateId) : null;
    EmailTemplate emailTemplate4 = (paramUserAction.endEmailTemplateId != null) ? this.emailTemplateMapper.retrieveById(paramUserAction.endEmailTemplateId) : null;
    UserAction userAction1 = (paramUserAction.id != null) ? this.userActionMapper.retrieveByIdIgnoreActive(paramUserAction.id) : null;
    UserAction userAction2 = (paramUserAction.name != null) ? this.userActionMapper.retrieveExisting(paramUserAction) : null;
    return (new Validator())
      .notBlank(paramUserAction.name, "userAction.name", new Object[0])
      .ifTrue(paramBoolean, paramValidator -> paramValidator.notDuplicate(paramUserAction1, "userActionId", new Object[] { paramUserAction2.id })).ifTrue(!paramBoolean, paramValidator -> paramValidator.notMissing(paramUserAction1.id, "userActionId", new Object[0]).notInactive(paramUserAction2, (), "userActionId", new Object[] { paramUserAction1.id })).notInactive(userAction2, paramUserAction -> Boolean.valueOf(paramUserAction.active), "userAction.name", new Object[] { paramUserAction.name }).ifNoErrors(paramValidator -> paramValidator.notDuplicate(paramUserAction1, "userAction.name", new Object[] { paramUserAction2.name })).forEach(paramUserAction.options, (paramValidator, paramUserActionOption, paramInteger) -> {
          paramValidator.notBlank(paramUserActionOption.name, "userAction.option.name", new Object[0]);
          if (paramUserActionOption.localizedNames != null)
            paramUserActionOption.localizedNames.forEach(()); 
        }).ifTrue((paramUserAction.localizedNames != null), paramValidator -> paramUserAction.localizedNames.forEach(()))



      
      .ifTrue((paramUserAction.userEmailingEnabled || paramUserAction.includeEmailInEventJSON), paramValidator -> paramValidator.notMissing(paramUserAction.startEmailTemplateId, "userAction.startEmailTemplateId", new Object[0]).ifLastCheckHadNoError(()).ifTrue(paramUserAction.temporal, ()))











      
      .ifTrue(paramUserAction.preventLogin, paramValidator -> paramValidator.ensure(paramUserAction.temporal, "userAction.temporal", "[invalid]", new Object[0]))
      .done();
  }
}
