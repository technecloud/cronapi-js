package cronapi.serialization;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class CronappModule extends SimpleModule {
  
  @Override
  public void setupModule(SetupContext context) {
    super.setupModule(context);
    context.addBeanSerializerModifier(new CronappBeanSerializerModifier());
  }
}
