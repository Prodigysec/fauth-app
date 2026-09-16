package io.fusionauth.app.service.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStep;
import io.fusionauth.domain.form.FormStepType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FormStepViewModel {
  public final List<FormField> fieldObjects;
  
  public final FormStepType type;
  
  public boolean skippable;
  
  @JsonProperty("generalMessagesSeen")
  private boolean generalMessagesSeen;
  
  @JsonProperty("hideFields")
  private List<String> hideFields = new ArrayList<>();
  
  public FormStepViewModel(FormStep paramFormStep) {
    this(paramFormStep.fieldObjects, paramFormStep.type);
  }
  
  public FormStepViewModel(List<FormField> paramList, FormStepType paramFormStepType) {
    this.fieldObjects = paramList;
    this.type = paramFormStepType;
    resetStep();
  }
  
  @JacksonConstructor
  private FormStepViewModel() {
    this.fieldObjects = null;
    this.type = null;
  }
  
  public void acknowledge() {
    if (this.generalMessagesSeen)
      throw new IllegalStateException("You cannot confirm a %s step that is already acknowledgeCompleted".formatted(new Object[] { this.type })); 
    this.generalMessagesSeen = true;
  }
  
  public boolean equals(Object paramObject) {
    FormStepViewModel formStepViewModel;
    if (paramObject instanceof FormStepViewModel) {
      formStepViewModel = (FormStepViewModel)paramObject;
    } else {
      return false;
    } 
    return (this.skippable == formStepViewModel.skippable && this.generalMessagesSeen == formStepViewModel.generalMessagesSeen && Objects.equals(this.fieldObjects, formStepViewModel.fieldObjects) && this.type == formStepViewModel.type && Objects.equals(this.hideFields, formStepViewModel.hideFields));
  }
  
  @JsonIgnore
  public List<String> getHideFields() {
    return this.hideFields;
  }
  
  public Optional<VerificationStrategy> getVerificationStrategy(Tenant paramTenant) {
    if (!isVerificationStep())
      return Optional.empty(); 
    switch (this.type) {
      default:
        throw new MatchException(null, null);
      case verifyEmail:
      
      case verifyPhoneNumber:
      
      case collectData:
        break;
    } 
    throw new IllegalArgumentException("collectData is not a pre-verification step");
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.fieldObjects, this.type, Boolean.valueOf(this.skippable), Boolean.valueOf(this.generalMessagesSeen), this.hideFields });
  }
  
  public void hideField(String paramString) {
    this.hideFields.add(paramString);
  }
  
  public boolean isGeneralMessagesSeen() {
    return this.generalMessagesSeen;
  }
  
  @JsonIgnore
  public boolean isVerificationStep() {
    return (this.type == FormStepType.verifyEmail || this.type == FormStepType.verifyPhoneNumber);
  }
  
  public void resetStep() {
    this.generalMessagesSeen = !isVerificationStep();
  }
  
  public void showField(String paramString) {
    if (!this.hideFields.remove(paramString))
      throw new IllegalArgumentException("Field %s is not currently hidden.".formatted(new Object[] { paramString })); 
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
