package io.fusionauth.api.domain.json.introspector;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import io.fusionauth.api.domain.json.annotation.MaskMapValue;
import io.fusionauth.api.domain.json.annotation.MaskString;

public class MaskAnnotationIntrospector extends JacksonAnnotationIntrospector {
  public Object findSerializer(Annotated paramAnnotated) {
    MaskString maskString = (MaskString)paramAnnotated.getAnnotation(MaskString.class);
    if (maskString != null)
      return new MaskStringSerializer(maskString.value(), maskString.showLast()); 
    MaskMapValue maskMapValue = (MaskMapValue)paramAnnotated.getAnnotation(MaskMapValue.class);
    if (maskMapValue != null) {
      if (maskMapValue.maskAll())
        return new MaskMapSerializer(maskMapValue.replacement()); 
      return new MapEntryMaskingSerializer(new MaskMapValue[] { maskMapValue });
    } 
    MaskMapValue.List list = (MaskMapValue.List)paramAnnotated.getAnnotation(MaskMapValue.List.class);
    if (list != null)
      return new MapEntryMaskingSerializer(list.value()); 
    return super.findSerializer(paramAnnotated);
  }
}
