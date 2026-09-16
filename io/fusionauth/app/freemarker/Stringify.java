package io.fusionauth.app.freemarker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import freemarker.ext.beans.SimpleMapModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;

public class Stringify implements TemplateMethodModelEx {
  private static final String ERROR_MESSAGE = "You must pass an object like this:\n\n  stringify(object)";
  
  private final ObjectMapper objectMapper;
  
  @Inject
  public Stringify(ObjectMapper paramObjectMapper) {
    this.objectMapper = paramObjectMapper;
  }
  
  public Object exec(List<Object> paramList) throws TemplateModelException {
    Object object;
    if (paramList.size() != 1)
      throw new TemplateModelException("You must pass an object like this:\n\n  stringify(object)"); 
    AdapterTemplateModel adapterTemplateModel = (AdapterTemplateModel)paramList.get(0);
    if (adapterTemplateModel instanceof SimpleMapModel) {
      SimpleMapModel simpleMapModel = (SimpleMapModel)paramList.get(0);
      object = simpleMapModel.getWrappedObject();
    } else {
      AdapterTemplateModel adapterTemplateModel1 = adapterTemplateModel;
      object = adapterTemplateModel1.getAdaptedObject(Object.class);
    } 
    try {
      return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    } catch (JsonProcessingException jsonProcessingException) {
      throw new TemplateModelException(jsonProcessingException);
    } 
  }
}
