package io.fusionauth.app.freemarker;

import freemarker.core.Environment;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.primeframework.mvc.freemarker.FieldSupportBeansWrapper;

public class FlattenUserData implements TemplateDirectiveModel {
  private static final String ERROR_MESSAGE = "You must pass a Map object and specify two loop variables that are used to output the property name and value. It should look something like this:\n\n  [@fusionAuth.flatten_data data=user.data; name, value]\n    ${name} : ${value}  [/@fusionAuth.flatten_data]";
  
  public void execute(Environment paramEnvironment, Map paramMap, TemplateModel[] paramArrayOfTemplateModel, TemplateDirectiveBody paramTemplateDirectiveBody) throws TemplateException, IOException {
    if (!paramMap.containsKey("data") || paramArrayOfTemplateModel.length != 2)
      throw new TemplateException("You must pass a Map object and specify two loop variables that are used to output the property name and value. It should look something like this:\n\n  [@fusionAuth.flatten_data data=user.data; name, value]\n    ${name} : ${value}  [/@fusionAuth.flatten_data]", paramEnvironment); 
    AdapterTemplateModel adapterTemplateModel = (AdapterTemplateModel)paramMap.get("data");
    Map map = (Map)adapterTemplateModel.getAdaptedObject(Map.class);
    outputProperty(paramEnvironment, paramArrayOfTemplateModel, paramTemplateDirectiveBody, map, "");
  }
  
  private void outputProperty(Environment paramEnvironment, TemplateModel[] paramArrayOfTemplateModel, TemplateDirectiveBody paramTemplateDirectiveBody, Object paramObject, String paramString) throws TemplateException, IOException {
    if (paramObject instanceof Map) {
      String str = paramString.equals("") ? "" : (paramString + ".");
      for (Map.Entry entry : ((Map)paramObject).entrySet())
        outputProperty(paramEnvironment, paramArrayOfTemplateModel, paramTemplateDirectiveBody, entry.getValue(), str + str); 
    } else if (paramObject instanceof List) {
      for (byte b = 0; b < ((List)paramObject).size(); b++)
        outputProperty(paramEnvironment, paramArrayOfTemplateModel, paramTemplateDirectiveBody, ((List)paramObject).get(b), paramString + "[" + paramString + "]"); 
    } else {
      paramArrayOfTemplateModel[0] = FieldSupportBeansWrapper.INSTANCE.wrap(paramString);
      paramArrayOfTemplateModel[1] = FieldSupportBeansWrapper.INSTANCE.wrap(paramObject);
      paramTemplateDirectiveBody.render(paramEnvironment.getOut());
    } 
  }
}
