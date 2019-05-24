package cronapi.odata.server;

import cronapi.database.TransactionManager;
import org.apache.olingo.odata2.api.ODataServiceFactory;
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
  private int order;

  public ODataServletV2(EntityManagerFactory entityManagerFactory, String namespace, int order) {
    this.entityManagerFactory = entityManagerFactory;
    this.namespace = namespace;
    this.order = order;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
    try {
      req.setAttribute(ODataServiceFactory.FACTORY_INSTANCE_LABEL, new JPAODataServiceFactory(this.entityManagerFactory, namespace, order));
      //TODO: Centralizar no cronapp filter e remover dos lugares com código duplicado
      try {
        super.service(req, res);
        TransactionManager.commit();
      } catch (Exception e) {
        TransactionManager.rollback();
        throw e;
      }
    } finally {
      ODataParameterizedWhereExpressionUtil.clear();
      ODataExpressionParser.clear();
      TransactionManager.close();
      TransactionManager.clear();
    }
  }
}
