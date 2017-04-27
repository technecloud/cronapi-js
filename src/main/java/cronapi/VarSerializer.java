package cronapi;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class VarSerializer extends StdSerializer<Var> {
  
  public VarSerializer() {
    super(Var.class);
  }
  
  @Override
  public void serialize(Var value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if(value != null)
      gen.writeObject(value.getObject());
    else
      gen.writeObject(null);
  }
}
