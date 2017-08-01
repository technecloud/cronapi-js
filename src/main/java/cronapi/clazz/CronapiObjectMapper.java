package cronapi.clazz;

import java.io.IOException;
import java.lang.reflect.Field;

import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class CronapiObjectMapper extends ObjectMapper {
  
  public Object convertToObject(Object fromValue, Class clazz) throws IllegalArgumentException {
    JavaType toValueType = _typeFactory.constructType(clazz);
    Class<?> targetType = toValueType.getRawClass();
    if(targetType != Object.class && !toValueType.hasGenericTypes() &&
            targetType.isAssignableFrom(fromValue.getClass())) {
      return fromValue;
    }
    
    // Then use TokenBuffer, which is a JsonGenerator:
    TokenBuffer buf = new TokenBuffer(this, false);
    if(isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
      buf = buf.forceUseOfBigDecimal(true);
    }
    try {
      // inlined 'writeValue' with minor changes:
      // first: disable wrapping when writing
      SerializationConfig config = getSerializationConfig().without(SerializationFeature.WRAP_ROOT_VALUE);
      // no need to check for closing of TokenBuffer
      _serializerProvider(config).serializeValue(buf, fromValue);
      
      // then matching read, inlined 'readValue' with minor mods:
      final JsonParser jp = buf.asParser();
      Object result;
      // ok to pass in existing feature flags; unwrapping handled by mapper
      final DeserializationConfig deserConfig = getDeserializationConfig();
      JsonToken t = _initForReading(jp);
      if(t == JsonToken.VALUE_NULL) {
        DeserializationContext ctxt = createDeserializationContext(jp, deserConfig);
        result = _findRootDeserializer(ctxt, toValueType).getNullValue(ctxt);
      }
      else if(t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
        result = null;
      }
      else { // pointing to event other than null
        DeserializationContext ctxt = createDeserializationContext(jp, deserConfig);
        JsonDeserializer<Object> deser = _findRootDeserializer(ctxt, toValueType);
        // note: no handling of unwarpping
        Field field = ReflectionUtils.findField(deser.getClass(), "_ignoreAllUnknown");
        field.setAccessible(true);
        try {
          field.set(deser, true);
        }
        catch(IllegalAccessException e) {
          //
        }
        result = deser.deserialize(jp, ctxt);
      }
      jp.close();
      return result;
    }
    catch(IOException e) { // should not occur, no real i/o...
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }
}
