package cronapi.database;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.google.gson.JsonObject;
import cronapi.QueryManager;
import cronapi.RestClient;
import cronapi.Utils;
import cronapi.Var;
import cronapi.i18n.Messages;
import cronapi.odata.server.DatasourceExtension;
import cronapi.odata.server.JPQLParserUtil;
import cronapi.rest.security.CronappSecurity;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmModel;
import org.eclipse.persistence.annotations.Multitenant;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorQueryManager;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.internal.jpa.jpql.HermesParser;
import org.eclipse.persistence.internal.jpa.metamodel.EntityTypeImpl;
import org.eclipse.persistence.internal.queries.ReportItem;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Class database manipulation, responsible for querying, inserting,
 * updating and deleting database data procedurally, allowing paged
 * navigation and setting page size.
 *
 * @author robson.ataide
 * @version 1.0
 * @since 2017-04-26
 */
public class DataSource implements JsonSerializable {

  private static final Logger log = LoggerFactory.getLogger(DataSource.class);

  private String entity;
  private String simpleEntity;
  private Class domainClass;
  private String filter;
  private Var[] params;
  private int pageSize;
  private Page page;
  private int index;
  private int current;
  private Pageable pageRequest;
  private Object insertedElement = null;
  private EntityManager customEntityManager;
  private DataSourceFilter dsFilter;
  private boolean multiTenant = true;
  private boolean plainData = false;
  private boolean useUrlParams = false;
  private boolean countData = false;
  private boolean useOdataRequest = false;
  private boolean isEor = false;
  private boolean useOffset = false;
  private DatabaseQuery queryParsed;

  /**
   * Init a datasource with a page size equals 100
   *
   * @param entity - full name of entitiy class like String
   */
  public DataSource(String entity) {
    this(entity, 100);
  }

  public DataSource(JsonObject query) {
    this(query.get("entityFullName").getAsString(), 100);
    QueryManager.checkMultiTenant(query, this);
  }

  /**
   * Init a datasource with a page size equals 100, and custom entity manager
   *
   * @param entity        - full name of entitiy class like String
   * @param entityManager - custom entity manager
   */
  public DataSource(String entity, EntityManager entityManager) {
    this(entity, 100);
    this.customEntityManager = entityManager;
  }

  /**
   * Init a datasource setting a page size
   *
   * @param entity   - full name of entitiy class like String
   * @param pageSize - page size of a Pageable object retrieved from repository
   */
  public DataSource(String entity, int pageSize) {
    CronappDescriptorQueryManager.enableMultitenant();
    this.entity = entity;
    this.simpleEntity = entity.substring(entity.lastIndexOf(".") + 1);
    this.pageSize = pageSize;
    this.pageRequest = PageRequest.of(0, pageSize);

    // initialize dependencies and necessaries objects
    this.instantiateRepository();
  }

  public EntityManager getEntityManager(Class domainClass) {
    EntityManager em;
    if (customEntityManager != null)
      em = customEntityManager;
    else
      em = TransactionManager.getEntityManager(domainClass);

    enableTenantToogle(em);

    return em;
  }

  public Class getDomainClass() {
    return domainClass;
  }

  public String getSimpleEntity() {
    return simpleEntity;
  }

  public String getEntity() {
    return entity;
  }

  /**
   * Retrieve repository from entity
   *
   * @throws RuntimeException when repository not fount, entity passed not found or cast repository
   */
  private void instantiateRepository() {
    try {
      domainClass = Class.forName(this.entity);
    } catch (ClassNotFoundException cnfex) {
      throw new RuntimeException(cnfex);
    }
  }

  private void enableTenantToogle(EntityManager em) {
    try {
      for (EntityType type : em.getMetamodel().getEntities()) {
        DescriptorQueryManager old = ((EntityTypeImpl) type).getDescriptor().getQueryManager();

        ClassDescriptor desc = ((EntityTypeImpl) type).getDescriptor();

        if (desc.getMultitenantPolicy() != null && !(desc.getMultitenantPolicy() instanceof CronappMultitenantPolicy)) {
          desc.setMultitenantPolicy(new CronappMultitenantPolicy(desc.getMultitenantPolicy()));
        }

        if (CronappDescriptorQueryManager.needProxy(old)) {
          desc.setQueryManager(CronappDescriptorQueryManager.build(old));
        }


      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void startMultitenant(EntityManager em) {
    if (!multiTenant) {
      CronappDescriptorQueryManager.disableMultitenant();
    }
  }

  private void endMultitetant() {
    if (!multiTenant) {
      CronappDescriptorQueryManager.enableMultitenant();
    }
  }

  public long count() {
    Long[] total = (Long[]) fetch(true);
    if (total != null && total.length > 0) {
      return total[0];
    }
    return 0;
  }

  /**
   * Retrieve objects from database using repository when filter is null or empty,
   * if filter not null or is not empty, this method uses entityManager and create a
   * jpql instruction.
   *
   * @return a array of Object
   */
  public Object[] fetch() {
    return fetch(false);
  }

  public Object[] fetch(boolean isCount) {

    this.isEor = false;
    String jpql = this.filter;
    Var[] params = this.params;

    if (jpql == null) {
      jpql = "select e from " + simpleEntity + " e";
    }

    boolean containsNoTenant = jpql.contains("/*notenant*/");
    jpql = jpql.replace("/*notenant*/", "");

    if (containsNoTenant) {
      multiTenant = false;
    }

    if (dsFilter != null) {
      dsFilter.applyTo(domainClass, jpql, params);
      params = dsFilter.getAppliedParams();
      jpql = dsFilter.getAppliedJpql();
    }

    try {

      boolean namedParams = (params.length > 0 && params[0].getId() != null) || useUrlParams;

      JPQLParserUtil.ODataInfo info = null;

      if (useOdataRequest) {
        info = JPQLParserUtil.addODdataRequest(jpql, params);
        if (info != null) {
          if (info.jpql != null) {
            jpql = info.jpql;
          }
          if (info.params != null) {
            params = info.params;
          }
        }
      }

      List<String> parsedParams = JPQLParserUtil.parseParams(jpql);

      int o = 0;
      for (String param : parsedParams) {
        jpql = jpql.replaceFirst(":" + param, ":param" + o);
        o++;
      }

      Map<String, Var> paramsValues = null;

      if (namedParams) {
        paramsValues = new LinkedHashMap<>();
        if (useUrlParams) {
          for (String key : parsedParams) {
            paramsValues.put(key, Var.valueOf(RestClient.getRestClient().getParameter(key)));
          }
        } else {
          for (Var p : params) {
            paramsValues.put(p.getId(), p);
          }
        }
      }

      EntityManager em = getEntityManager(domainClass);

      startMultitenant(em);

      AbstractSession session = (AbstractSession) ((EntityManagerImpl) em.getDelegate()).getActiveSession();
      DatabaseQuery dbQuery = EJBQueryImpl.buildEJBQLDatabaseQuery("customQuery", jpql, session, (Enum) null, (Map) null, session.getDatasourcePlatform().getConversionManager().getLoader());

      HermesParser parser = new HermesParser();
      DatabaseQuery queryParsed = parser.buildQuery(jpql, session);

      TypedQuery<?> query = new EJBQueryImpl(dbQuery, (EntityManagerImpl) em.getDelegate());

      List<Class> argsTypes = queryParsed.getArgumentTypes();
      List<String> argsNames = queryParsed.getArguments();

      for (String name : argsNames) {
        query.setParameter(name, null);
      }

      if (namedParams) {

        for (int i = 0; i < parsedParams.size(); i++) {
          String paramName = "param" + i;
          String realParamName = parsedParams.get(i);

          if (paramsValues != null) {
            Var value = paramsValues.get(realParamName);
            if (value != null) {
              int idx = argsNames.indexOf(paramName);
              query.setParameter(paramName, value.getObject(argsTypes.get(idx)));
            }
          }
        }
      } else {

        for (int i = 0; i < parsedParams.size(); i++) {
          String param = "param" + i;
          Var p = null;
          if (i <= params.length - 1) {
            p = params[i];
          }
          if (p != null) {
            int idx = argsNames.indexOf(param);
            query.setParameter(param, p.getObject(argsTypes.get(idx)));
          } else {
            query.setParameter(param, null);
          }
        }
      }

      if ((this.pageRequest != null) && (!isCount)) {
        if (info != null && info.first != null) {
          query.setFirstResult(info.first);
        } else if (this.useOffset) {
          query.setFirstResult(this.pageRequest.getPageNumber());
        } else {
          query.setFirstResult(this.pageRequest.getPageNumber() * this.pageRequest.getPageSize());
        }
        if (info != null && info.max != null) {
          query.setMaxResults(info.max);
        } else {
          query.setMaxResults(this.pageRequest.getPageSize());
        }
      }

      if (isCount) {
        return new Long[]{JPQLParserUtil.countAsLong(jpql, query, em)};
      }

      List<?> resultsInPage = query.getResultList();

      if (plainData && !isCount) {
        String context = this.entity.substring(0, this.entity.indexOf("."));
        JPAEdmModel model = new JPAEdmModel(em.getMetamodel(), context);
        model.getBuilder().build();

        DatasourceExtension extension = new DatasourceExtension((EntityManagerImpl) em, 1);
        extension.jpql(jpql);
        extension.extendJPAEdmSchema(model.getEdmSchemaView().getEdmSchema());

        resultsInPage = normalizeList(resultsInPage, extension.getJpqlEntity());
      }

      Long total = 0L;
      if (!isCount) {
        if (countData) {
          Object[] countResult = fetch(true);
          if (!ArrayUtils.isEmpty(countResult)) {
            total = (Long) countResult[0];
          }
        }

        this.queryParsed = queryParsed;
      }

      this.page = new PageImpl(resultsInPage, this.pageRequest, total);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      enableMultiTenant();
    }

    // has data, moves cursor to first position
    if (this.page.getNumberOfElements() > 0) {
      this.current = 0;
    }

    if (isCount) {
      return new Long[]{(long) this.page.getContent().get(0)};
    } else {
      return this.page.getContent().toArray();
    }
  }

  private List normalizeList(List entities, org.apache.olingo.odata2.api.edm.provider.EntityType entityType) {
    if (entities != null && !entities.isEmpty()) {
      try {
        List<Object> newEntities = new LinkedList<>();

        for (Object obj : entities) {
          if (Objects.isNull(obj)) {
            continue;
          }

          Var entity = Var.valueOf(new LinkedHashMap<>());
          if (obj.getClass().isArray()) {
            int i = 0;

            for (Object o : (Object[]) obj) {
              JPAEdmMappingImpl mapping = (JPAEdmMappingImpl) entityType.getProperties().get(i).getMapping();
              String key = entityType.getProperties().get(i).getName();
              if (mapping != null && mapping.isPath()) {
                Var sub;
                if (mapping.getComplexIndex() != -1) {
                  sub = Var.valueOf(((Object[]) obj)[mapping.getComplexIndex()]);
                } else {
                  sub = Var.valueOf(o);
                }
                entity.set(key, sub.get(mapping.getPath()));
              } else {
                entity.set(key, o);
              }

              i++;
            }
          } else {
            if (entityType.getProperties().size() == 1) {
              String key = entityType.getProperties().get(0).getName();
              entity.set(key, obj);
            } else {
              Var sub = Var.valueOf(obj);
              for (Property property : entityType.getProperties()) {
                JPAEdmMappingImpl mapping = (JPAEdmMappingImpl) property.getMapping();
                String key = property.getName();
                entity.set(key, sub.get(mapping.getInternalName()));
              }
            }
          }

          newEntities.add(entity);
        }

        entities = newEntities;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    return entities;
  }

  public EntityMetadata getMetadata() {
    return new EntityMetadata(domainClass);
  }

  /**
   * Create a new instance of entity and add a
   * results and set current (index) for his position
   */
  public void insert() {
    try {
      this.insertedElement = this.domainClass.newInstance();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public Object toObject(Map<?, ?> values) {
    try {
      Object insertedElement = this.domainClass.newInstance();
      for (Object key : values.keySet()) {
        Utils.updateFieldOnFiltered(insertedElement, key.toString(), values.get(key));
      }

      return insertedElement;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public void insert(Object value) {
    try {
      if (value instanceof Var)
        value = ((Var) value).getObject();

      if (value instanceof Map) {
        this.insertedElement = this.domainClass.newInstance();
        Map<?, ?> values = (Map<?, ?>) value;
        for (Object key : values.keySet()) {
          try {
            updateField(key.toString(), values.get(key));
          } catch (Exception e) {
            // Abafa campo não encontrado
          }
        }
      } else {
        this.insertedElement = value;
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public Object save() {
    return save(true);
  }

  private void processCloudFields(Object toSaveParam) {
    Object toSave;
    if (toSaveParam != null) {
      toSave = toSaveParam;
    } else if (this.insertedElement != null) {
      toSave = this.insertedElement;
    } else {
      toSave = this.getObject();
    }
    if (toSave != null) {
      Utils.processCloudFields(toSave);
    }
  }

  /**
   * Saves the object in the current index or a new object when has insertedElement
   */
  public Object save(boolean returnCursorAfterInsert) {
    try {
      processCloudFields(null);
      Object toSave;
      Object saved;

      EntityManager em = getEntityManager(domainClass);
      try {
        startMultitenant(em);

        if (!em.getTransaction().isActive()) {
          em.getTransaction().begin();
        }

        if (this.insertedElement != null) {
          toSave = this.insertedElement;
          if (returnCursorAfterInsert)
            this.insertedElement = null;
          em.persist(toSave);
        } else
          toSave = this.getObject();

        saved = em.merge(toSave);

        if (toSave.getClass().getAnnotation(Multitenant.class) != null) {
          em.flush();
          if (multiTenant) {
            em.refresh(toSave);
          }
        }
      } finally {
        endMultitetant();
      }
      return saved;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Object saveAfterCommit(AbstractSession session) {
    try {
      Object toSave;

      if (this.insertedElement != null) {
        toSave = this.insertedElement;
        session.insertObject(toSave);
      } else
        toSave = this.getObject();

      return toSave;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void delete(Var[] primaryKeys) {
    insert();
    int i = 0;
    Var[] params = new Var[primaryKeys.length];

    EntityManager em = getEntityManager(domainClass);
    EntityType type = em.getMetamodel().entity(domainClass);

    String jpql = " DELETE FROM " + entity.substring(entity.lastIndexOf(".") + 1) + " WHERE ";
    List<TypeKey> keys = getKeys(type);

    boolean first = true;
    for (TypeKey key : keys) {
      if (!first) {
        jpql += " AND ";
      }
      first = false;
      jpql += "" + key.name + " = :p" + i;
      params[i] = Var.valueOf("p" + i, primaryKeys[i].getObject(key.field.getType().getJavaType()));
      i++;
    }

    execute(jpql, params);
  }

  /**
   * Removes the object in the current index
   */
  public void delete() {
    EntityManager em = getEntityManager(domainClass);
    try {
      Object toRemove = this.getObject();

      startMultitenant(em);

      if (!em.getTransaction().isActive()) {
        em.getTransaction().begin();
      }
      // returns managed instance
      toRemove = em.merge(toRemove);
      em.remove(toRemove);
      if (!multiTenant) {
        em.flush();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      endMultitetant();
    }
  }

  /**
   * Update a field from object in the current index
   *
   * @param fieldName  - attributte name in entity
   * @param fieldValue - value that replaced or inserted in field name passed
   */
  public void updateField(String fieldName, Object fieldValue) {
    Utils.updateFieldOnFiltered(getObject(), fieldName, fieldValue);
  }


  /**
   * Update fields from object in the current index
   *
   * @param fields - bidimensional array like fields
   *               sample: { {"name", "Paul"}, {"age", "21"} }
   * @thows RuntimeException if a field is not accessible through a set method
   */
  public void updateFields(Var... fields) {
    for (Var field : fields) {
      updateField(field.getId(), field.getObject());
    }
  }

  public void filterByPk(Var[] params) {
    filter(null, PageRequest.of(1, 1), params);
  }

  public void filter(Var data, Var[] extraParams) {

    EntityManager em = getEntityManager(domainClass);
    EntityType type = em.getMetamodel().entity(domainClass);

    int i = 0;
    String jpql = " select e FROM " + entity.substring(entity.lastIndexOf(".") + 1) + " e WHERE ";
    Vector<Var> params = new Vector<>();
    for (Object obj : JPAUtil.getAjustedAttributes(type)) {
      SingularAttribute field = (SingularAttribute) obj;
      if (field.isId()) {
        if (i > 0) {
          jpql += " AND ";
        }
        jpql += "e." + field.getName() + " = :p" + i;
        params.add(Var.valueOf("p" + i, data.getField(field.getName()).getObject(field.getType().getJavaType())));
        i++;
      }
    }

    if (extraParams != null) {
      for (Var p : extraParams) {
        jpql += "e." + p.getId() + " = :p" + i;
        params.add(Var.valueOf("p" + i, p.getObject()));
        i++;
      }
    }

    Var[] arr = params.toArray(new Var[params.size()]);

    filter(jpql, arr);
  }

  public void update(Var data) {
    try {
      List<String> fieldsByteHeaderSignature = cronapi.Utils.getFieldsWithAnnotationByteHeaderSignature(domainClass);
      LinkedList<String> fields = data.keySet();
      for (String key : fields) {
        if (!fieldsByteHeaderSignature.contains(key) || isFieldByteWithoutHeader(key, data.getField(key))) {
          if (!key.equalsIgnoreCase(Class.class.getSimpleName())) {
            try {
              this.updateField(key, data.getField(key));
            } catch (Exception e) {
              //NoCommand
            }
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isFieldByteWithoutHeader(String fieldName, Object fieldValue) {
    //Verificando se não tem o header, se não tiver o header, houve alteração, então tem que atualizar.
    boolean result = false;


    if (fieldValue instanceof Var) {
      if (((Var) fieldValue).getObject() == null)
        return true;
      else if (cronapi.util.StorageService.isTempFileJson(((Var) fieldValue).getObject().toString()))
        return true;
    }

    Method setMethod = Utils.findMethod(getObject(), "set" + fieldName);
    if (setMethod != null) {
      if (fieldValue instanceof Var) {
        fieldValue = ((Var) fieldValue).getObject(setMethod.getParameterTypes()[0]);
      } else {
        Var tVar = Var.valueOf(fieldValue);
        fieldValue = tVar.getObject(setMethod.getParameterTypes()[0]);
      }
      Object header = cronapi.util.StorageService.getFileBytesMetadata((byte[]) fieldValue);
      result = (header == null);
    }

    return result;
  }

  /**
   * Return object in current index
   *
   * @return Object from database in current position
   */
  public Object getObject() {

    if (this.insertedElement != null)
      return this.insertedElement;

    if (this.current < 0 || this.current > this.page.getContent().size() - 1)
      return null;

    return this.page.getContent().get(this.current);
  }

  /**
   * Return field passed from object in current index
   *
   * @return Object value of field passed
   * @thows RuntimeException if a field is not accessible through a set method
   */
  public Object getObject(String fieldName) {
    try {
      Method getMethod = Utils.findMethod(getObject(), "get" + fieldName);
      if (getMethod != null)
        return getMethod.invoke(getObject());
      return null;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Moves the index for next position, in pageable case,
   * looking for next page and so on
   */
  public void next() {

    if (this.isEor) {
      this.current = -1;
      return;
    }

    if (this.page.getNumberOfElements() > (this.current + 1)) {
      this.current++;
    }
    else {
      this.pageRequest = PageRequest.of(this.page.getNumber() + 1, pageSize);
      this.fetch();
      if (this.page.getNumberOfElements() > 0) {
        this.current = 0;
      }
      else {
        this.current = -1;
        this.isEor = true;
      }
    }
  }

  /**
   * Moves the index for next position, in pageable case,
   * looking for next page and so on
   */
  public void nextOnPage() {
    this.current++;
  }

  /**
   * Verify if can moves the index for next position,
   * in pageable case, looking for next page and so on
   *
   * @return boolean true if has next, false else
   */
  public boolean hasNext() {
    if (this.page.getNumberOfElements() > (this.current + 1))
      return true;
    else {
      if (this.page.hasNext()) {
        return true;
      } else {
        return false;
      }
    }
  }

  public boolean hasData() {
    return getObject() != null;
  }

  /**
   * Moves the index for previous position, in pageable case,
   * looking for next page and so on
   *
   * @return boolean true if has previous, false else
   */
  public boolean previous() {
    if (this.current - 1 >= 0) {
      this.current--;
    } else {
      if (this.page.hasPrevious()) {
        this.pageRequest = this.page.previousPageable();
        this.fetch();
        this.current = this.page.getNumberOfElements() - 1;
      } else {
        return false;
      }
    }
    return true;
  }

  public void setCurrent(int current) {
    this.current = current;
  }

  public int getCurrent() {
    return this.current;
  }

  /**
   * Gets a Pageable object retrieved from repository
   *
   * @return pageable from repository, returns null when fetched by filter
   */
  public Page getPage() {
    return this.page;
  }

  /**
   * Create a new page request with size passed
   *
   * @param pageSize size of page request
   */
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
    this.pageRequest = PageRequest.of(0, pageSize);
    this.current = -1;
  }

  /**
   * Fetch objects from database by a filter
   *
   * @param filter jpql instruction like a namedQuery
   * @param params parameters used in jpql instruction
   */
  public void filter(String filter, Var... params) {
    this.filter = filter;
    this.params = params;
    this.pageRequest = PageRequest.of(0, pageSize);
    this.current = -1;
    this.fetch();
  }

  public void setDataSourceFilter(DataSourceFilter dsFilter) {
    this.dsFilter = dsFilter;
  }

  public void filter(String filter, PageRequest pageRequest, Var... params) {
    if (filter == null) {
      if (params.length > 0) {
        EntityManager em = getEntityManager(domainClass);
        EntityType type = em.getMetamodel().entity(domainClass);

        int i = 0;
        String jpql = "Select e from " + simpleEntity + " e where (";
        for (Object obj : JPAUtil.getAjustedAttributes(type)) {
          SingularAttribute field = (SingularAttribute) obj;
          if (field.isId()) {
            if (i > 0) {
              jpql += " and ";
            }
            jpql += "e." + field.getName() + " = :p" + i;
            params[i].setId("p" + i);
            i++;
          }
        }
        jpql += ")";

        filter = jpql;
      } else {
        filter = "Select e from " + simpleEntity + " e ";
      }
    } else {
      //Verificar se existe parametros que são ID´s
      List<String> parsedParams = JPQLParserUtil.parseParams(filter);
      if (params.length > parsedParams.size() && domainClass != null) {
        String alias = JPQLConverter.getAliasFromSql(filter);
        EntityManager em = getEntityManager(domainClass);
        EntityType type = em.getMetamodel().entity(domainClass);
        int i = 0;
        String filterForId = " (";
        for (Object obj : JPAUtil.getAjustedAttributes(type)) {
          SingularAttribute field = (SingularAttribute) obj;
          if (field.isId()) {
            if (i > 0) {
              filterForId += " and ";
            }
            filterForId += alias + "." + field.getName() + " = :id" + i;
            i++;
          }
        }
        filterForId += ")";
        if (filter.toLowerCase().indexOf("where") > -1)
          filterForId = " and " + filterForId;
        else
          filterForId = " where " + filterForId;
        filter = Utils.addFilterInSQLClause(filter, filterForId);
      }
    }

    this.params = params;
    this.filter = filter;
    this.pageRequest = pageRequest;
    this.current = -1;
    this.fetch();
  }

  private Class forName(String name) {
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private Object newInstance(String name) {
    try {
      return Class.forName(name).newInstance();
    } catch (Exception e) {
      return null;
    }
  }

  private static class TypeKey {
    String name;
    SingularAttribute field;
  }

  private void addKeys(EntityManager em, EntityType type, String parent, List<TypeKey> keys) {
    for (Object obj : JPAUtil.getAjustedAttributes(type)) {
      SingularAttribute field = (SingularAttribute) obj;
      if (field.isId()) {
        if (field.getType().getPersistenceType() == Type.PersistenceType.BASIC) {
          TypeKey key = new TypeKey();
          key.name = parent == null ? field.getName() : parent + "." + field.getName();
          key.field = field;

          keys.add(key);
        } else {
          EntityType subType = (EntityType) field.getType();
          addKeys(em, subType, (parent == null ? field.getName() : parent + "." + field.getName()), keys);
        }
      }
    }
  }

  private List<TypeKey> getKeys(EntityType type) {
    EntityManager em = getEntityManager(domainClass);
    List<TypeKey> keys = new LinkedList<>();
    addKeys(em, type, null, keys);
    return keys;
  }

  public void deleteRelation(String refId, Var[] primaryKeys, Var[] relationKeys) {
    EntityMetadata metadata = getMetadata();
    RelationMetadata relationMetadata = metadata.getRelations().get(refId);

    EntityManager em = getEntityManager(domainClass);
    int i = 0;

    String jpql = null;

    Var[] params = null;
    if (relationMetadata.getAssossiationName() != null) {
      params = new Var[relationKeys.length + primaryKeys.length];

      jpql = " DELETE FROM " + relationMetadata.gettAssossiationSimpleName() + " WHERE ";
      EntityType type = em.getMetamodel().entity(domainClass);

      List<TypeKey> keys = getKeys(type);

      for (TypeKey key : keys) {
        if (i > 0) {
          jpql += " AND ";
        }

        jpql += relationMetadata.getAssociationAttribute().getName() + "." + key.name + " = :p" + i;
        params[i] = Var.valueOf("p" + i, primaryKeys[i].getObject(key.field.getType().getJavaType()));
        i++;
      }

      int v = 0;
      type = em.getMetamodel().entity(forName(relationMetadata.getAssossiationName()));
      keys = getKeys(type);

      for (TypeKey key : keys) {
        if (i > 0) {
          jpql += " AND ";
        }
        jpql += relationMetadata.getAttribute().getName() + "." + key.name + " = :p" + i;
        params[i] = Var.valueOf("p" + i, relationKeys[v].getObject(key.field.getType().getJavaType()));
        i++;
        v++;
      }

    } else {
      params = new Var[relationKeys.length];

      jpql = " DELETE FROM " + relationMetadata.getSimpleName() + " WHERE ";
      EntityType type = em.getMetamodel().entity(forName(relationMetadata.getName()));

      List<TypeKey> keys = getKeys(type);

      for (TypeKey key : keys) {
        if (i > 0) {
          jpql += " AND ";
        }

        jpql += "" + key.name + " = :p" + i;

        params[i] = Var.valueOf("p" + i, relationKeys[i].getObject(key.field.getType().getJavaType()));
        i++;
      }
    }

    execute(jpql, params);
  }

  public Object insertRelation(String refId, Map<?, ?> data, Var... primaryKeys) {
    EntityMetadata metadata = getMetadata();
    RelationMetadata relationMetadata = metadata.getRelations().get(refId);

    EntityManager em = getEntityManager(domainClass);

    Object result = null;
    try {
      startMultitenant(em);

      filter(null, PageRequest.of(0, 100), primaryKeys);
      Object insertion = null;
      if (relationMetadata.getAssossiationName() != null) {
        insertion = this.newInstance(relationMetadata.getAssossiationName());
        Utils.updateFieldOnFiltered(insertion, relationMetadata.getAttribute().getName(),
            Var.valueOf(data).getObject(forName(relationMetadata.getName())));
        Utils.updateFieldOnFiltered(insertion, relationMetadata.getAssociationAttribute().getName(), getObject());
        result = getObject();
      } else {
        insertion = Var.valueOf(data).getObject(forName(relationMetadata.getName()));
        Utils.updateFieldOnFiltered(insertion, relationMetadata.getAttribute().getName(), getObject());
        result = insertion;
      }

      processCloudFields(insertion);
      if (!em.getTransaction().isActive()) {
        em.getTransaction().begin();
      }

      em.persist(insertion);
      if (!multiTenant) {
        em.flush();
      }
    } finally {
      endMultitetant();
    }
    return result;
  }

  public void resolveRelation(String refId) {
    EntityMetadata metadata = getMetadata();
    RelationMetadata relationMetadata = metadata.getRelations().get(refId);

    if (relationMetadata.getAssossiationName() != null) {
      try {
        domainClass = Class.forName(relationMetadata.getAttribute().getJavaType().getName());
      } catch (ClassNotFoundException e) {
        //
      }

    } else {
      try {
        domainClass = Class.forName(relationMetadata.getName());
      } catch (ClassNotFoundException e) {
        //
      }
    }
  }

  public void filterByRelation(String refId, PageRequest pageRequest, Var... primaryKeys) {
    EntityMetadata metadata = getMetadata();
    RelationMetadata relationMetadata = metadata.getRelations().get(refId);

    EntityManager em = getEntityManager(domainClass);

    EntityType type = null;
    String name = null;
    String selectAttr = "";
    String filterAttr = relationMetadata.getAttribute().getName();
    type = em.getMetamodel().entity(domainClass);

    if (relationMetadata.getAssossiationName() != null) {
      name = relationMetadata.gettAssossiationSimpleName();
      selectAttr = "." + relationMetadata.getAttribute().getName();
      filterAttr = relationMetadata.getAssociationAttribute().getName();

      try {
        domainClass = Class.forName(relationMetadata.getAttribute().getJavaType().getName());
      } catch (ClassNotFoundException e) {
        //
      }

    } else {
      name = relationMetadata.getSimpleName();

      try {
        domainClass = Class.forName(relationMetadata.getName());
      } catch (ClassNotFoundException e) {
        //
      }
    }

    int i = 0;
    String jpql = "Select e" + selectAttr + " from " + name + " e where ";
    for (Object obj : JPAUtil.getAjustedAttributes(type)) {
      SingularAttribute field = (SingularAttribute) obj;
      if (field.isId()) {
        if (i > 0) {
          jpql += " and ";
        }
        jpql += "e." + filterAttr + "." + field.getName() + " = :p" + i;
        primaryKeys[i].setId("p" + i);
      }
    }

    filter(jpql, pageRequest, primaryKeys);

  }

  /**
   * Clean Datasource and to free up allocated memory
   */
  public void clear() {
    this.pageRequest = PageRequest.of(0, 100);
    this.current = -1;
    this.page = null;
  }

  /**
   * Execute Query
   *
   * @param query  - JPQL instruction for filter objects to remove
   * @param params - Bidimentional array with params name and params value
   */
  public void execute(String query, Var... params) {
    EntityManager em = getEntityManager(domainClass);
    try {
      startMultitenant(em);
      try {
        boolean namedParams = (params.length > 0 && params[0].getId() != null) || useUrlParams;

        List<String> parsedParams = JPQLParserUtil.parseParams(query);
        List<String> nonWhereParams = JPQLParserUtil.getNonWhereParams(query);

        int o = 0;
        for (String param : parsedParams) {
          query = query.replaceFirst(":" + param, ":param" + o);
          o++;
        }

        TypedQuery<?> strQuery = em.createQuery(query, domainClass);
        AbstractSession session = (AbstractSession) ((EntityManagerImpl) em.getDelegate()).getActiveSession();

        Map<String, Var> paramsValues = null;

        if (namedParams) {
          paramsValues = new LinkedHashMap<>();
          if (useUrlParams) {
            for (String key : parsedParams) {
              paramsValues.put(key, Var.valueOf(RestClient.getRestClient().getParameter(key)));
            }
          } else {
            for (Var p : params) {
              paramsValues.put(p.getId(), p);
            }
          }
        }

        HermesParser parser = new HermesParser();
        DatabaseQuery queryParsed = parser.buildQuery(query, session);

        List<Class> argsTypes = queryParsed.getArgumentTypes();
        List<String> argsNames = queryParsed.getArguments();

        for (String name : argsNames) {
          strQuery.setParameter(name, null);
        }

        Object instanceForUpdate = this.domainClass.newInstance();

        if (namedParams) {
          for (int i = 0; i < parsedParams.size(); i++) {
            String paramName = "param" + i;
            String realParamName = parsedParams.get(i);

            if (paramsValues != null) {
              Var value = paramsValues.get(realParamName);
              if (value != null) {
                int idx = argsNames.indexOf(paramName);
                strQuery.setParameter(paramName, value.getObject(argsTypes.get(idx)));
                if (nonWhereParams.contains(realParamName)) {
                  try {
                    Utils.updateField(instanceForUpdate, realParamName, value.getObject(argsTypes.get(idx)));
                  } catch (Exception e) {
                    log.error(e.getMessage(), e);
                  }
                }
              }
            }
          }
        } else {

          for (int i = 0; i < parsedParams.size(); i++) {
            String param = "param" + i;
            Var p = null;
            if (i <= params.length - 1) {
              p = params[i];
            }
            if (p != null) {
              int idx = argsNames.indexOf(param);
              strQuery.setParameter(param, p.getObject(argsTypes.get(idx)));
              if (nonWhereParams.contains(parsedParams.get(i))) {
                try {
                  Utils.updateField(instanceForUpdate, parsedParams.get(i), p.getObject(argsTypes.get(idx)));
                } catch (Exception e) {
                  log.error(e.getMessage(), e);
                }
              }
            } else {
              strQuery.setParameter(param, null);
            }
          }
        }

        processCloudFields(instanceForUpdate);

        try {
          if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
          }
          strQuery.executeUpdate();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }

      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    } finally {
      endMultitetant();
    }
  }

  public Var getTotalElements() {
    return new Var(this.page.getTotalElements());
  }

  @Override
  public String toString() {
    if (this.page != null) {
      return this.page.getContent().toString();
    } else {
      return "[]";
    }
  }

  @Override
  public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (queryParsed instanceof ReportQuery) {
      gen.writeStartArray();
      for (Object row : this.page.getContent()) {
        if (row.getClass().isArray()) {
          Object[] array = (Object[]) row;
          gen.writeStartObject();
          int i = 0;
          for (ReportItem item : ((ReportQuery) queryParsed).getItems()) {
            String name = item.getName();

            if (name == null || name.isEmpty()) {
              name = "expression";
            }
            gen.writeFieldName(name);
            gen.writeObject(array[i]);
            i++;
          }
          gen.writeEndObject();
        }
      }
      gen.writeEndArray();
    } else {
      gen.writeObject(this.page.getContent());
    }
  }

  @Override
  public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
      throws IOException {
    gen.writeObject(this.page.getContent());
  }

  public void checkRESTSecurity(String method) throws Exception {
    checkRESTSecurity(domainClass, method);
  }

  public void checkRESTSecurity(String relationId, String method) throws Exception {
    EntityMetadata metadata = getMetadata();
    RelationMetadata relationMetadata = metadata.getRelations().get(relationId);

    checkRESTSecurity(Class.forName(relationMetadata.getName()), method);
  }

  public String getRelationEntity(String relationId) throws Exception {
    EntityMetadata metadata = getMetadata();
    RelationMetadata relationMetadata = metadata.getRelations().get(relationId);
    return relationMetadata.getName();
  }

  private void checkRESTSecurity(Class clazz, String method) throws Exception {
    Annotation security = clazz.getAnnotation(CronappSecurity.class);
    boolean authorized = false;

    if (security instanceof CronappSecurity) {
      CronappSecurity cronappSecurity = (CronappSecurity) security;
      Method methodPermission = cronappSecurity.getClass().getMethod(method.toLowerCase());
      if (methodPermission != null) {
        String value = (String) methodPermission.invoke(cronappSecurity);
        if (value == null || value.trim().isEmpty()) {
          value = "authenticated";
        }

        String[] authorities = value.trim().split(";");

        for (String role : authorities) {
          if (role.equalsIgnoreCase("authenticated")) {
            authorized = RestClient.getRestClient().getUser() != null;
            if (authorized)
              break;
          }
          if (role.equalsIgnoreCase("permitAll") || role.equalsIgnoreCase("public")) {
            authorized = true;
            break;
          }
          for (GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
            if (role.equalsIgnoreCase(authority.getAuthority())) {
              authorized = true;
              break;
            }
          }

          if (authorized)
            break;
        }
      }
    }

    if (!authorized) {
      throw new RuntimeException(Messages.getString("notAllowed"));
    }
  }

  public void disableMultiTenant() {
    this.multiTenant = false;
  }

  public void enableMultiTenant() {
    this.multiTenant = true;
  }

  public String getFilter() {
    return this.filter;
  }

  public Var getIds() {
    if (getObject() == null) {
      return Var.VAR_NULL;
    }

    EntityManager em = getEntityManager(domainClass);
    EntityType type = em.getMetamodel().entity(domainClass);

    LinkedList result = new LinkedList<>();

    for (Object obj : JPAUtil.getAjustedAttributes(type)) {
      SingularAttribute field = (SingularAttribute) obj;
      if (field.isId()) {
        result.add(getObject(field.getName()));
      }
    }

    return Var.valueOf(result);
  }

  public Var getId() {
    if (getObject() == null) {
      return Var.VAR_NULL;
    }

    EntityManager em = getEntityManager(domainClass);
    EntityType type = em.getMetamodel().entity(domainClass);

    for (Object obj : JPAUtil.getAjustedAttributes(type)) {
      SingularAttribute field = (SingularAttribute) obj;
      if (field.isId()) {
        return Var.valueOf(getObject(field.getName()));
      }
    }

    return Var.VAR_NULL;
  }

  public Object getObjectWithId(Var[] ids) {
    EntityManager em = getEntityManager(domainClass);
    EntityType type = em.getMetamodel().entity(domainClass);
    Object instanceDomain = null;
    try {
      instanceDomain = domainClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    int i = 0;
    for (Object obj : JPAUtil.getAjustedAttributes(type)) {
      SingularAttribute field = (SingularAttribute) obj;
      if (field.isId()) {
        Utils.updateField(instanceDomain, field.getName(), ids[i].getObject(field.getType().getJavaType()));
        i++;
      }
    }
    return instanceDomain;
  }

  public void validate(String jpql) {
    EntityManager em = getEntityManager(domainClass);

    AbstractSession session = (AbstractSession) ((EntityManagerImpl) em.getDelegate()).getActiveSession();

    HermesParser parser = new HermesParser();
    try {
      parser.buildQuery(jpql, session);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isPlainData() {
    return plainData;
  }

  public void setPlainData(boolean plainData) {
    this.plainData = plainData;
  }

  public boolean useUrlParams() {
    return useUrlParams;
  }

  public void setUseUrlParams(boolean useUrlParams) {
    this.useUrlParams = useUrlParams;
  }

  public void flush() {
    EntityManager em = getEntityManager(domainClass);
    em.flush();
  }

  public void setUseOdataRequest(boolean useOdataRequest) {
    this.useOdataRequest = useOdataRequest;
  }

  public void setUseOffset(boolean useOffset) {
    this.useOffset = useOffset;
  }
}
