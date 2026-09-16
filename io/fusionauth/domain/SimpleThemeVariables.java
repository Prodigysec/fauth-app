package io.fusionauth.domain;

import io.fusionauth.domain.html.Favicon;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SimpleThemeVariables implements Buildable<SimpleThemeVariables> {
  public String alertBackgroundColor;
  
  public String alertFontColor;
  
  public URI backgroundImageURL;
  
  public String backgroundSize;
  
  public String borderRadius;
  
  public String deleteButtonColor;
  
  public String deleteButtonFocusColor;
  
  public String deleteButtonTextColor;
  
  public String deleteButtonTextFocusColor;
  
  public String errorFontColor;
  
  public String errorIconColor;
  
  public List<Favicon> favicons;
  
  public String fontColor;
  
  public String fontFamily;
  
  public boolean footerDisplay;
  
  public String iconBackgroundColor;
  
  public String iconColor;
  
  public String infoIconColor;
  
  public String inputBackgroundColor;
  
  public String inputIconColor;
  
  public String inputTextColor;
  
  public String linkTextColor;
  
  public String linkTextFocusColor;
  
  public String logoImageSize;
  
  public URI logoImageURL;
  
  public String monoFontColor;
  
  public String monoFontFamily;
  
  public String pageBackgroundColor;
  
  public String panelBackgroundColor;
  
  public String primaryButtonColor;
  
  public String primaryButtonFocusColor;
  
  public String primaryButtonTextColor;
  
  public String primaryButtonTextFocusColor;
  
  public SimpleThemeVariables() {}
  
  public SimpleThemeVariables(SimpleThemeVariables paramSimpleThemeVariables) {
    this.alertBackgroundColor = paramSimpleThemeVariables.alertBackgroundColor;
    this.alertFontColor = paramSimpleThemeVariables.alertFontColor;
    this.backgroundImageURL = paramSimpleThemeVariables.backgroundImageURL;
    this.backgroundSize = paramSimpleThemeVariables.backgroundSize;
    this.borderRadius = paramSimpleThemeVariables.borderRadius;
    this.deleteButtonColor = paramSimpleThemeVariables.deleteButtonColor;
    this.deleteButtonFocusColor = paramSimpleThemeVariables.deleteButtonFocusColor;
    this.deleteButtonTextColor = paramSimpleThemeVariables.deleteButtonTextColor;
    this.deleteButtonTextFocusColor = paramSimpleThemeVariables.deleteButtonTextFocusColor;
    this.errorFontColor = paramSimpleThemeVariables.errorFontColor;
    this.errorIconColor = paramSimpleThemeVariables.errorIconColor;
    this.favicons = (paramSimpleThemeVariables.favicons == null) ? null : (List<Favicon>)paramSimpleThemeVariables.favicons.stream().map(Favicon::new).collect(Collectors.toList());
    this.fontColor = paramSimpleThemeVariables.fontColor;
    this.fontFamily = paramSimpleThemeVariables.fontFamily;
    this.footerDisplay = paramSimpleThemeVariables.footerDisplay;
    this.iconBackgroundColor = paramSimpleThemeVariables.iconBackgroundColor;
    this.iconColor = paramSimpleThemeVariables.iconColor;
    this.infoIconColor = paramSimpleThemeVariables.infoIconColor;
    this.inputBackgroundColor = paramSimpleThemeVariables.inputBackgroundColor;
    this.inputIconColor = paramSimpleThemeVariables.inputIconColor;
    this.inputTextColor = paramSimpleThemeVariables.inputTextColor;
    this.linkTextColor = paramSimpleThemeVariables.linkTextColor;
    this.linkTextFocusColor = paramSimpleThemeVariables.linkTextFocusColor;
    this.logoImageSize = paramSimpleThemeVariables.logoImageSize;
    this.logoImageURL = paramSimpleThemeVariables.logoImageURL;
    this.monoFontColor = paramSimpleThemeVariables.monoFontColor;
    this.monoFontFamily = paramSimpleThemeVariables.monoFontFamily;
    this.pageBackgroundColor = paramSimpleThemeVariables.pageBackgroundColor;
    this.panelBackgroundColor = paramSimpleThemeVariables.panelBackgroundColor;
    this.primaryButtonColor = paramSimpleThemeVariables.primaryButtonColor;
    this.primaryButtonFocusColor = paramSimpleThemeVariables.primaryButtonFocusColor;
    this.primaryButtonTextColor = paramSimpleThemeVariables.primaryButtonTextColor;
    this.primaryButtonTextFocusColor = paramSimpleThemeVariables.primaryButtonTextFocusColor;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    SimpleThemeVariables simpleThemeVariables = (SimpleThemeVariables)paramObject;
    return (Objects.equals(this.alertBackgroundColor, simpleThemeVariables.alertBackgroundColor) && 
      Objects.equals(this.alertFontColor, simpleThemeVariables.alertFontColor) && 
      Objects.equals(this.backgroundImageURL, simpleThemeVariables.backgroundImageURL) && 
      Objects.equals(this.backgroundSize, simpleThemeVariables.backgroundSize) && 
      Objects.equals(this.borderRadius, simpleThemeVariables.borderRadius) && 
      Objects.equals(this.deleteButtonColor, simpleThemeVariables.deleteButtonColor) && 
      Objects.equals(this.deleteButtonTextColor, simpleThemeVariables.deleteButtonTextColor) && 
      Objects.equals(this.deleteButtonTextFocusColor, simpleThemeVariables.deleteButtonTextFocusColor) && 
      Objects.equals(this.deleteButtonFocusColor, simpleThemeVariables.deleteButtonFocusColor) && 
      Objects.equals(this.errorFontColor, simpleThemeVariables.errorFontColor) && 
      Objects.equals(this.errorIconColor, simpleThemeVariables.errorIconColor) && 
      Objects.equals(this.favicons, simpleThemeVariables.favicons) && 
      Objects.equals(this.fontColor, simpleThemeVariables.fontColor) && 
      Objects.equals(this.fontFamily, simpleThemeVariables.fontFamily) && 
      Objects.equals(Boolean.valueOf(this.footerDisplay), Boolean.valueOf(simpleThemeVariables.footerDisplay)) && 
      Objects.equals(this.iconBackgroundColor, simpleThemeVariables.iconBackgroundColor) && 
      Objects.equals(this.iconColor, simpleThemeVariables.iconColor) && 
      Objects.equals(this.infoIconColor, simpleThemeVariables.infoIconColor) && 
      Objects.equals(this.inputBackgroundColor, simpleThemeVariables.inputBackgroundColor) && 
      Objects.equals(this.inputIconColor, simpleThemeVariables.inputIconColor) && 
      Objects.equals(this.inputTextColor, simpleThemeVariables.inputTextColor) && 
      Objects.equals(this.linkTextColor, simpleThemeVariables.linkTextColor) && 
      Objects.equals(this.linkTextFocusColor, simpleThemeVariables.linkTextFocusColor) && 
      Objects.equals(this.logoImageSize, simpleThemeVariables.logoImageSize) && 
      Objects.equals(this.logoImageURL, simpleThemeVariables.logoImageURL) && 
      Objects.equals(this.monoFontColor, simpleThemeVariables.monoFontColor) && 
      Objects.equals(this.monoFontFamily, simpleThemeVariables.monoFontFamily) && 
      Objects.equals(this.pageBackgroundColor, simpleThemeVariables.pageBackgroundColor) && 
      Objects.equals(this.panelBackgroundColor, simpleThemeVariables.panelBackgroundColor) && 
      Objects.equals(this.primaryButtonColor, simpleThemeVariables.primaryButtonColor) && 
      Objects.equals(this.primaryButtonFocusColor, simpleThemeVariables.primaryButtonFocusColor) && 
      Objects.equals(this.primaryButtonTextColor, simpleThemeVariables.primaryButtonTextColor) && 
      Objects.equals(this.primaryButtonTextFocusColor, simpleThemeVariables.primaryButtonTextFocusColor));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.alertBackgroundColor, this.alertFontColor, this.backgroundImageURL, this.backgroundSize, this.borderRadius, this.deleteButtonColor, this.deleteButtonTextColor, this.deleteButtonTextFocusColor, this.deleteButtonFocusColor, this.errorFontColor, 
          this.errorIconColor, this.favicons, this.fontColor, this.fontFamily, 

          
          Boolean.valueOf(this.footerDisplay), this.iconBackgroundColor, this.iconColor, this.infoIconColor, this.inputBackgroundColor, this.inputIconColor, 
          this.inputTextColor, this.linkTextColor, this.linkTextFocusColor, this.logoImageSize, this.logoImageURL, this.monoFontColor, this.monoFontFamily, this.pageBackgroundColor, this.panelBackgroundColor, this.primaryButtonColor, 
          this.primaryButtonFocusColor, this.primaryButtonTextColor, this.primaryButtonTextFocusColor });
  }
}
