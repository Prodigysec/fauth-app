package io.fusionauth.app.action.ajax.user.consent;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.Family;
import io.fusionauth.domain.FamilyMember;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserConsent;
import io.fusionauth.domain.api.ConsentResponse;
import io.fusionauth.domain.api.FamilyResponse;
import io.fusionauth.domain.api.UserConsentRequest;
import io.fusionauth.domain.api.UserConsentResponse;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.user.SearchResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class AddAction extends BaseAJAXAction {
  public Consent consent;
  
  public UUID consentId;
  
  public List<Consent> consents;
  
  public User user;
  
  public UserConsent userConsent = new UserConsent();
  
  public UUID userId;
  
  public List<User> users = new ArrayList<>();
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    prepareForm();
    this.userConsent.userId = this.userId;
    this.userConsent.consentId = this.consentId;
    UserConsent userConsent = ((UserConsentResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createUserConsent(null, new UserConsentRequest(this.userConsent)))).userConsent;
    writeAuditLog("Created user consent with Id [" + String.valueOf(userConsent.id) + "] for User with Id [" + String.valueOf(userConsent.userId) + "] granted by User with Id [" + String.valueOf(userConsent.giverUserId) + "]");
    return "success";
  }
  
  @FormPrepareMethod
  public void prepareForm() {
    if (this.consents == null) {
      this.consents = ((ConsentResponse)this.delegate.execute(FusionAuthClient::retrieveConsents)).consents;
      List list1 = (List)Objects.requireNonNullElseGet(((UserConsentResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserConsents(this.userId))).userConsents, Collections::emptyList);
      List list2 = (List)list1.stream().map(paramUserConsent -> paramUserConsent.consentId).collect(Collectors.toList());
      this.consents.removeIf(paramConsent -> paramList.contains(paramConsent.id));
    } 
    if (this.userId != null)
      this.user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user; 
    if (this.consentId != null && this.consent == null) {
      List list1 = (List)Objects.requireNonNullElseGet(((FamilyResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveFamilies(this.userId))).families, Collections::emptyList);
      List list2 = (List)list1.stream().flatMap(paramFamily -> paramFamily.members.stream()).filter(paramFamilyMember -> paramFamilyMember.owner).map(paramFamilyMember -> paramFamilyMember.userId).collect(Collectors.toList());
      this.consent = ((ConsentResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveConsent(this.consentId))).consent;
      List<?> list = Collections.emptyList();
      if (!list2.isEmpty()) {
        list = ((SearchResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchUsersByIds(paramList))).users;
        list.sort(Comparator.comparing(paramUser -> paramUser.birthDate));
      } 
      if (this.user != null && this.consent.canSelfConsent(this.user)) {
        this.users.add(this.user);
        for (User user : list) {
          if (user.id.equals(this.user.id))
            continue; 
          this.users.add(user);
        } 
      } else {
        this.users.addAll(list);
      } 
    } 
  }
  
  @ValidationMethod
  public void validate() {
    if (this.consentId == null)
      this.frontEndSupport.addFieldError("consentId", "[missing]consentId", new Object[0]); 
  }
}
