package cronapi;

import cronapi.rest.CronapiWS;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.xml.ws.Endpoint;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CronapiBeanConfigurator {

  public static Map<String, Var> INIT_PARAMS = new HashMap<>();

  @Autowired
  ServletContext context;

  @Autowired
  private Bus bus;

  @Bean
  public ServletContext getServletContext() {
    Enumeration<String> names = context.getInitParameterNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      INIT_PARAMS.put(name, Var.valueOf(context.getInitParameter(name)));
    }
    return context;
  }

  @Bean
  public Endpoint endpointCronapiWS() {
    EndpointImpl endpoint = new EndpointImpl(bus, new CronapiWS());
    endpoint.publish("cronapi");
    return endpoint;
  }
}
