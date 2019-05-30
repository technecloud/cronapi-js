package cronapi;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CronapiBeanConfigurator {

  public static Map<String, Var> INIT_PARAMS = new HashMap<>();

  @Autowired
  ServletContext context;

  @Bean
  public ServletContext getServletContext() {
    Enumeration<String> names = context.getInitParameterNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      INIT_PARAMS.put(name, Var.valueOf(context.getInitParameter(name)));
    }
    return context;
  }
}
