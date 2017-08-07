package cronapi;

import java.io.IOException;
import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.CalendarSerializer;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.gson.JsonElement;

@Configuration
public class CronapiConfigurator {

  public static String ENCODING = "UTF-8";

  @Autowired
  private HttpServletRequest request;

  @Bean
  public Jackson2ObjectMapperBuilder objectMapperBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    builder.serializationInclusion(JsonInclude.Include.NON_NULL);
    builder.deserializerByType(Var.class, new VarDeserializer());
    builder.serializerByType(JsonElement.class, new ToStringSerializer());

    builder.serializerByType(Calendar.class, new CalendarSerializer() {

      @Override
      public void serialize(Calendar value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if ("true".equals(request.getHeader("toJS")) || "true".equals(request.getParameter("toJS")))
          gen.writeRawValue("new Date(\""+ISO8601Utils.format(value.getTime(), true)+"\")");
        else
          gen.writeString(ISO8601Utils.format(value.getTime(), true));
      }
    });
    builder.dateFormat(new ISO8601DateFormat() {
      @Override
      public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        String value = ISO8601Utils.format(date, true);
        toAppendTo.append(value);
        return toAppendTo;
      }
    });

    builder.filters(new SimpleFilterProvider()
        .setDefaultFilter(new SimpleBeanPropertyFilter() {
          @Override
          protected boolean include(PropertyWriter writer) {
            if (writer instanceof BeanPropertyWriter && request != null) {
              if (request.getAttribute("BeanPropertyFilter") != null) {
                HashSet<String> ignores = (HashSet<String>) request.getAttribute("BeanPropertyFilter");
                String name = ((BeanPropertyWriter) writer).getMember().getDeclaringClass().getName() + "#" + writer.getName();
                if (ignores.contains(name))
                  return false;
              }
            }
            return true;
          }
        }));
    return builder;
  }
}
