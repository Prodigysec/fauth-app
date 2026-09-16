package io.fusionauth.app.freemarker;

import com.google.inject.Inject;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class StaticsModel implements TemplateHashModel {
  private final TemplateHashModel hashModel;
  
  @Inject
  public StaticsModel(Configuration paramConfiguration) {
    this.hashModel = ((BeansWrapper)paramConfiguration.getObjectWrapper()).getStaticModels();
  }
  
  public TemplateModel get(String paramString) throws TemplateModelException {
    return this.hashModel.get(paramString);
  }
  
  public boolean isEmpty() throws TemplateModelException {
    return this.hashModel.isEmpty();
  }
}
