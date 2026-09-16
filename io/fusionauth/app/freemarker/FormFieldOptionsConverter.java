package io.fusionauth.app.freemarker;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;
import io.fusionauth.app.action.BaseThemedAction;
import io.fusionauth.domain.form.FormDataType;
import io.fusionauth.domain.form.FormField;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FormFieldOptionsConverter implements TemplateMethodModelEx {
  private static final String ERROR_MESSAGE = "You must pass a single argument to this method of type FormField. This function is only intended for converting field options for a Form Field.\n  Example usage: convert_themed_field_options(field, theme)\n";
  
  private final BiFunction<FormDataType, String, Object> convert;
  
  public FormFieldOptionsConverter() {
    this.convert = ((paramFormDataType, paramString) -> {
        switch (paramFormDataType) {
          case bool:
          
          case date:
          
          case number:
          
        } 
        return paramString;
      });
  }
  
  public Object exec(List<TemplateModel> paramList) throws TemplateModelException {
    if (paramList.size() != 2)
      throw new TemplateModelException("You must pass a single argument to this method of type FormField. This function is only intended for converting field options for a Form Field.\n  Example usage: convert_themed_field_options(field, theme)\n"); 
    FormField formField = (FormField)DeepUnwrap.unwrap(paramList.get(0));
    BaseThemedAction.LocaleResolvedCachedTheme localeResolvedCachedTheme1 = (BaseThemedAction.LocaleResolvedCachedTheme)DeepUnwrap.unwrap(paramList.get(1));
    Objects.requireNonNull(localeResolvedCachedTheme1);
    BaseThemedAction.LocaleResolvedCachedTheme localeResolvedCachedTheme2 = localeResolvedCachedTheme1;
    return formField.options.stream().collect(Collectors.toMap(paramString -> this.convert.apply(paramFormField.type, paramString), paramString -> paramLocaleResolvedCachedTheme.optionalMessage(paramString, new Object[0]), (paramString1, paramString2) -> paramString2, java.util.LinkedHashMap::new));
  }
}
