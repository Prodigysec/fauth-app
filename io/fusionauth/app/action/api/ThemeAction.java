package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import io.fusionauth.api.service.system.ThemeService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.ThemeRequest;
import io.fusionauth.domain.api.ThemeResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{themeId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class ThemeAction extends BaseAPIAction implements Patchable {
  private final ThemeService themeService;
  
  @JSONPatch
  @JSONRequest
  public ThemeRequest request = new ThemeRequest();
  
  @JSONResponse
  public ThemeResponse response;
  
  @PreParameter
  public UUID themeId;
  
  private ThemeService.ValidationResult result;
  
  @Inject
  public ThemeAction(FrontEndSupport paramFrontEndSupport, ThemeService paramThemeService) {
    super(paramFrontEndSupport);
    this.themeService = paramThemeService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.themeService.delete(this.result.existing);
    return "success";
  }
  
  public String get() {
    if (this.themeId == null) {
      this.response = new ThemeResponse(this.themeService.retrieveAll());
    } else {
      if (this.result.existing == null)
        return "missing"; 
      this.response = new ThemeResponse(this.result.existing);
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.themeId != null)
      this.request.theme = this.themeService.retrieveById(this.themeId); 
  }
  
  public String post() {
    this.themeService.create(this.result.theme);
    this.response = new ThemeResponse(this.result.theme);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.themeService.update(this.result.existing, this.result.theme);
    this.response = new ThemeResponse(this.result.theme);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    this.result = this.themeService.validateDelete(this.themeId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.themeId != null) {
      this.result = this.themeService.validateRetrieve(this.themeId);
      this.frontEndSupport.transfer(this.result.errors);
    } 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.theme == null) {
      this.frontEndSupport.addFieldError("theme", "[missing]theme", new Object[0]);
      return;
    } 
    this.request.theme.id = this.themeId;
    this.request.theme.normalize();
    if (this.request.sourceThemeId != null) {
      this.result = this.themeService.validateCopy(this.request.theme, this.request.sourceThemeId);
    } else {
      this.result = this.themeService.validateCreate(this.request.theme);
    } 
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.request.theme == null) {
      this.frontEndSupport.addFieldError("theme", "[missing]theme", new Object[0]);
      return;
    } 
    this.request.theme.id = this.themeId;
    this.request.theme.normalize();
    this.result = this.themeService.validateUpdate(this.request.theme, this.request.sourceThemeId);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
