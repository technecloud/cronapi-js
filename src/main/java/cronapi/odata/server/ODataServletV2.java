package cronapi.odata.server;

import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.core.servlet.ODataServlet;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class ODataServletV2 extends ODataServlet {

  private static final long serialVersionUID = 1L;
  private EntityManagerFactory entityManagerFactory;
  private String namespace;

  ODataServletV2(EntityManagerFactory entityManagerFactory, String namespace) {
    this.entityManagerFactory = entityManagerFactory;
    this.namespace = namespace;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
    req.setAttribute(ODataServiceFactory.FACTORY_INSTANCE_LABEL, new JpaOdataServiceFactory(this.entityManagerFactory, namespace));
    super.service(req, res);
  }

}
