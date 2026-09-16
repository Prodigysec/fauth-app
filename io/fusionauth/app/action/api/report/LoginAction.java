package io.fusionauth.app.action.api.report;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.IntervalCount;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.user.IdentityTypeHelper;
import io.fusionauth.api.service.user.IdentityTypeValidator;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.time.TimeUtils;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.DisplayableRawLogin;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.RawLogin;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.report.Count;
import io.fusionauth.domain.api.report.LoginReportResponse;
import io.fusionauth.domain.search.LoginRecordSearchCriteria;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(scheme = {"api"}, requiresAuthentication = true)
public class LoginAction extends BaseTenantAPIAction {
  private final LoginMapper loginMapper;
  
  private final BiConsumer<RawLogin, Map<Integer, Integer>> rawToInterval;
  
  private final UserReaderService userReader;
  
  public UUID applicationId;
  
  public ZonedDateTime end;
  
  public String loginId;
  
  public List<String> loginIdTypes;
  
  @JSONResponse
  public LoginReportResponse response;
  
  public ZonedDateTime start;
  
  public UUID userId;
  
  @Inject
  public LoginAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, LoginMapper paramLoginMapper, UserReaderService paramUserReaderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.rawToInterval = ((paramRawLogin, paramMap) -> {
        int i = TimeUtils.toHour(paramRawLogin.instant);
        paramMap.putIfAbsent(Integer.valueOf(i), Integer.valueOf(0));
        paramMap.put(Integer.valueOf(i), Integer.valueOf(((Integer)paramMap.get(Integer.valueOf(i))).intValue() + 1));
      });
    this.loginIdTypes = new ArrayList<>();
    this.response = new LoginReportResponse();
    this.loginMapper = paramLoginMapper;
    this.userReader = paramUserReaderService;
  }
  
  public String get() {
    List<IntervalCount> list;
    ZonedDateTime zonedDateTime1 = this.start.withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
    ZonedDateTime zonedDateTime2 = this.end.withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
    int i = TimeUtils.toHour(zonedDateTime1);
    int j = TimeUtils.toHour(zonedDateTime2);
    if (this.userId != null) {
      list = buildUserReport();
    } else {
      list = (this.applicationId != null) ? this.loginMapper.retrieveHourlyLogins(this.applicationId, i, j) : this.loginMapper.retrieveGlobalHourlyLogins(i, j);
    } 
    list.sort(Comparator.comparingInt(paramIntervalCount -> paramIntervalCount.period));
    this.response

      
      .total = list.stream().peek(paramIntervalCount -> this.response.hourlyCounts.add(new Count(paramIntervalCount.count, paramIntervalCount.period))).mapToLong(paramIntervalCount -> paramIntervalCount.count).sum();
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateUserExists() {
    Validator validator = (new Validator()).notMissing(this.end, "end", new Object[0]).notMissing(this.start, "start", new Object[0]);
    if (validator.hasErrors()) {
      Objects.requireNonNull(this.frontEndSupport);
      validator.done(this.frontEndSupport::transfer);
      return;
    } 
    if (this.loginId != null) {
      Objects.requireNonNull(this.frontEndSupport);
      validator.validate(paramValidator -> IdentityTypeValidator.validate(paramValidator, this.loginIdTypes, "loginIdTypes")).ifNoErrors(() -> {
            List<IdentityType> list = IdentityTypeHelper.convert(this.loginIdTypes);
            User user = this.userReader.retrieveByLoginId((getTenant()).id, this.loginId, list);
            if (user == null)
              throw new NotFoundException(); 
            this.userId = user.id;
          }).done(this.frontEndSupport::transfer);
    } else if (this.userId != null) {
      User user = this.userReader.retrieveById(getOptionalTenantId(), this.userId);
      if (user == null)
        throw new NotFoundException(); 
    } 
  }
  
  private List<IntervalCount> buildUserReport() {
    List<DisplayableRawLogin> list;
    int i = 0;
    HashMap<Object, Object> hashMap = new HashMap<>();
    char c = 'Ϩ';
    do {
      int j = i;
      list = this.loginMapper.retrieveRawLoginsByCriteria((new LoginRecordSearchCriteria()).with(paramLoginRecordSearchCriteria -> paramLoginRecordSearchCriteria.userId = this.userId)
          .with(paramLoginRecordSearchCriteria -> paramLoginRecordSearchCriteria.startRow = paramInt)
          .with(paramLoginRecordSearchCriteria -> paramLoginRecordSearchCriteria.numberOfResults = paramInt));
      list.stream()
        
        .filter(paramDisplayableRawLogin -> (this.applicationId == null || this.applicationId.equals(paramDisplayableRawLogin.applicationId)))
        .forEach(paramDisplayableRawLogin -> this.rawToInterval.accept(paramDisplayableRawLogin, paramMap));
      i += c;
    } while (list.size() >= c);
    return (List<IntervalCount>)hashMap.entrySet()
      .stream()
      .map(paramEntry -> new IntervalCount(this.applicationId, ((Integer)paramEntry.getValue()).intValue(), 0, ((Integer)paramEntry.getKey()).intValue()))
      .collect(Collectors.toList());
  }
}
