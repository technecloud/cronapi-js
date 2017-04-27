package cronapi;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class VarDeserializer extends StdDeserializer<Var> {
  
  private JsonDeserializer objectDeserializer = JsonNodeDeserializer.getDeserializer(Object.class);
  
  private ObjectMapper mapper = new ObjectMapper();
  
  public VarDeserializer() {
    super(Var.class);
  }
  
  @Override
  public Var deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    Object o = objectDeserializer.deserialize(p, ctxt);
    
    if(o instanceof ObjectNode) {
      o = mapper.convertValue(o, Map.class);
    }
    else if(o instanceof ArrayNode) {
      o = mapper.convertValue(o, List.class);
    }
    
    return new Var(o);
  }
}
