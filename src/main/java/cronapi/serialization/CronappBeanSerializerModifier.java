package cronapi.serialization;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.google.gson.JsonElement;

import cronapi.QueryManager;
import cronapi.RestClient;
import cronapi.SecurityBeanFilter;
import cronapi.Var;

public class CronappBeanSerializerModifier extends BeanSerializerModifier {
  
  @Override
  public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc,
                                            JsonSerializer<?> serializer) {
    if(serializer instanceof BeanSerializer)
      return new UserEventDeserializer((BeanSerializerBase)serializer);
    else
      return serializer;
  }
  
  public class UserEventDeserializer extends BeanSerializer {
    
    protected UserEventDeserializer(BeanSerializerBase src) {
      super(src);
    }
    
    @Override
    protected void serializeFieldsFiltered(Object bean, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
      serializeFields(bean, gen, provider);
    }
    
    @Override
    protected void serializeFields(Object bean, JsonGenerator gen, SerializerProvider provider)
            throws IOException, JsonGenerationException {
      final BeanPropertyWriter[] props;
      
      List<BeanPropertyWriter> addProperties = null;
      
      LinkedHashMap<String, JsonElement> newProperties = null;
      
      Class clazzToCheck = null;
      if(bean != null) {
        clazzToCheck = bean.getClass();
      }
      
      if(RestClient.getRestClient() != null && RestClient.getRestClient().getRequest() != null) {
        newProperties = (LinkedHashMap<String, JsonElement>)RestClient.getRestClient().getRequest()
                .getAttribute("NewBeanProperty");
      }
      
      if(newProperties != null && _props.length > 0 && bean != null) {
        for(Map.Entry<String, JsonElement> entry : newProperties.entrySet()) {
          if(entry.getKey().startsWith(bean.getClass().getCanonicalName() + ".")) {
            String field = entry.getKey().substring(entry.getKey().lastIndexOf(".") + 1);
            Var value = Var.VAR_NULL;
            
            if(entry.getValue().isJsonPrimitive()) {
              value = Var.valueOf(entry.getValue());
            }
            else {
              try {
                value = QueryManager.doExecuteBlockly(entry.getValue().getAsJsonObject(),
                        RestClient.getRestClient().getMethod(), Var.valueOf(bean));
              }
              catch(Exception e) {
                value = Var.valueOf("ERROR: " + e.getMessage());
              }
            }
            
            if(addProperties == null)
              addProperties = new LinkedList<>();
            
            BeanPropertyWriter beanPropertyWriter = new CronappVirtualBeanPropertyWriter(_props[0], field, value);
            addProperties.add(beanPropertyWriter);
          }
        }
      }
      
      if(addProperties != null) {
        props = Arrays.copyOf(_props, _props.length + addProperties.size());
        
        for(int i = 0; i < addProperties.size(); i++) {
          props[_props.length + i] = addProperties.get(i);
        }
      }
      else {
        props = _props;
      }
      
      int i = 0;
      try {
        for(final int len = props.length; i < len; ++i) {
          if(SecurityBeanFilter.includeProperty(clazzToCheck, props[i].getName(), "GET")) {
            BeanPropertyWriter prop = props[i];
            if(prop != null) { // can have nulls in filtered list
              prop.serializeAsField(bean, gen, provider);
            }
          }
        }
        if(_anyGetterWriter != null) {
          _anyGetterWriter.getAndSerialize(bean, gen, provider);
        }
      }
      catch(Exception e) {
        String name = (i == props.length) ? "[anySetter]" : props[i].getName();
        wrapAndThrow(provider, e, bean, name);
      }
      catch(StackOverflowError e) {
        JsonMappingException mapE = new JsonMappingException("Infinite recursion (StackOverflowError)", e);
        String name = (i == props.length) ? "[anySetter]" : props[i].getName();
        mapE.prependPath(new JsonMappingException.Reference(bean, name));
        throw mapE;
      }
    }
  }
}
