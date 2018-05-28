package cronapi.odata.server;

import cronapi.util.Operations;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.core.servlet.ODataServlet;
import org.apache.olingo.odata2.jpa.processor.core.ODataExpressionParser;
import org.apache.olingo.odata2.jpa.processor.core.ODataParameterizedWhereExpressionUtil;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class ODataServletV2 extends ODataServlet {

  private static final long serialVersionUID = 1L;
  private EntityManagerFactory entityManagerFactory;
  private String namespace;
  private JpaOdataServiceFactory factory;

  ODataServletV2(EntityManagerFactory entityManagerFactory, String namespace) {
    this.entityManagerFactory = entityManagerFactory;
    this.namespace = namespace;
    this.factory = new JpaOdataServiceFactory(this.entityManagerFactory, namespace);
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
    try {
      req.setAttribute(ODataServiceFactory.FACTORY_INSTANCE_LABEL, Operations.IS_DEBUG ? new JpaOdataServiceFactory(this.entityManagerFactory, namespace) : this.factory);
      super.service(req, res);
    } finally {
      ODataParameterizedWhereExpressionUtil.clear();
      ODataExpressionParser.clear();
    }
  }

}
