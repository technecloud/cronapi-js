package cronapi.database;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.RestClient;
import cronapi.Var;
import cronapi.odata.server.JPQLParserUtil;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.internal.jpa.QueryImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Classe que representa operações de acesso ao banco
 *
 * @author Robson Ataíde
 * @version 1.0
 * @since 2017-05-05
 *
 */
@CronapiMetaData(category = CategoryType.DATABASE, categoryTags = { "Database", "Banco", "Dados", "Storage" })
public class Operations {

  @CronapiMetaData(name = "{{datasourceQuery}}", nameTags = { "datasourceQuery", "openConnection",
      "abrirConsulta" }, description = "{{functionToQueryInDatasource}}", params = { "{{entity}}", "{{query}}",
          "{{paramsQueryTuples}}" }, paramsType = { ObjectType.STRING, ObjectType.STRING,
              ObjectType.LIST }, returnType = ObjectType.DATASET, arbitraryParams = true, wizard = "procedures_sql_callreturn")
  public static Var query(Var entity, Var query, Var ... params) {
    DataSource ds = new DataSource(entity.getObjectAsString());
    if(query == Var.VAR_NULL) {
      ds.fetch();
    } else {
      ds.filter(query.getObjectAsString(), params);
    }
    return new Var(ds);
  }

  public static Var queryPaged(Var entity, Var query, Var useRequestData, Var... params) {
    DataSource ds = new DataSource(entity.getObjectAsString());
    String limit = null;
    String offset = null;

    List<Var> finalParams = new LinkedList<>();
    for (Var param : params) {
      switch (param.getId()) {
        case "limit":
          limit = param.getObjectAsString();
          break;
        case "offset":
          offset = param.getObjectAsString();
          break;
        default:
          finalParams.add(param);
          break;
      }
    }

    PageRequest page;
    int pageNumber = 0;
    int pageSize = 100;

    if (offset != null) {
      pageNumber =  Integer.parseInt(offset);
      ds.setUseOffset(true);
    }
    if (limit != null) {
      pageSize =  Integer.parseInt(limit);
    }

    page = PageRequest.of(pageNumber, pageSize);

    if (useRequestData.getObjectAsBoolean()) {
      if (query != Var.VAR_NULL) {
        String queryString = RestClient.getRestClient().getRequest().getServletPath();
        if (queryString.contains("/api/cronapi/query/")) {
          queryString = queryString.replace("/api/cronapi/query/", "");
          String[] splitedQueryString = queryString.split("/");
          if (splitedQueryString.length > 1) {
            for (int ix = 1; ix < splitedQueryString.length; ix++) {
              Var param = Var.valueOf("id"+(ix-1), splitedQueryString[ix]);
              finalParams.add(param);
            }
          }
        }
      }
      String pageFromRequest = RestClient.getRestClient().getRequest().getParameter("page");
      boolean isODataParam = StringUtils.isNotEmpty(RestClient.getRestClient().getRequest().getParameter("$skip"));
      if (StringUtils.isEmpty(pageFromRequest)) {
        pageFromRequest = RestClient.getRestClient().getRequest().getParameter("$skip");
        if (StringUtils.isEmpty(pageFromRequest)) {
          pageFromRequest = "0";
        }
      }

      String pageSizeFromRequest = RestClient.getRestClient().getRequest().getParameter("size");
      if (StringUtils.isEmpty(pageSizeFromRequest)) {
        pageSizeFromRequest = RestClient.getRestClient().getRequest().getParameter("$top");
        if (StringUtils.isEmpty(pageSizeFromRequest)) {
          pageSizeFromRequest = "100";
        }
      }

      if (isODataParam) {
        pageFromRequest = pageFromRequest.equals("0") ? "0" : String.valueOf((Integer.parseInt(pageSizeFromRequest) / Integer.parseInt(pageFromRequest)) );
      }
      page = PageRequest.of(pageNumber, pageSize);
    }

    ds.setUseOdataRequest(useRequestData.getObjectAsBoolean());
    if (query == Var.VAR_NULL) {
      ds.fetch();
    } else {
      ds.filter(query.getObjectAsString(), page, finalParams.toArray(new Var[0]));
    }
    return new Var(ds);
  }

  @CronapiMetaData(type = "function", name = "{{datasourceNext}}", nameTags = { "next", "avançar",
      "proximo" }, description = "{{functionToMoveCursorToNextPosition}}", params = {
          "{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.VOID, displayInline = true)
  public static void next(Var ds) {
    ((DataSource)ds.getObject()).next();
  }

  @CronapiMetaData(type = "function", name = "{{datasourceHasData}}", nameTags = { "hasElement", "existeRegistro",
      "temRegistro" }, description = "{{functionToVerifyDataInCurrentPosition}}", params = {
          "{{datasource}}" }, paramsType = {
              ObjectType.DATASET }, returnType = ObjectType.BOOLEAN, displayInline = true)
  public static Var hasElement(Var ds) {
    if (ds.getObject() != null) {
      return Var.valueOf(((DataSource) ds.getObject()).getObject() != null);
    }

    return Var.VAR_FALSE;
  }

  @CronapiMetaData(type = "function", name = "{{datasourceClose}}", nameTags = { "close", "fechar", "limpar",
      "clear" }, description = "{{functionToCloseAndCleanDatasource}}", params = {
          "{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.VOID, displayInline = true)
  public static void close(Var ds) {
    ((DataSource)ds.getObject()).clear();
  }

  @CronapiMetaData(type = "function", name = "{{datasourceUpdateField}}", nameTags = { "updateField", "atualizarCampo",
      "setField", "modificarCampo" }, description = "{{functionToUpdateFieldInDatasource}}", params = {
          "{{datasource}}", "{{fieldName}}", "{{fieldValue}}" }, paramsType = { ObjectType.DATASET, ObjectType.STRING,
              ObjectType.STRING }, returnType = ObjectType.VOID)
  public static void updateField(Var ds, Var fieldName, Var fieldValue) {
    ds.setField(fieldName.getObjectAsString(), fieldValue.getObjectAsString());
  }

  @CronapiMetaData(type = "function", name = "{{datasourceGetActiveData}}", nameTags = { "getElement",
      "obterElemento" }, description = "{{functionToDatasourceGetActiveData}}", params = {
          "{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.OBJECT)
  public static Var getActiveData(Var ds) {
    return new Var(((DataSource)ds.getObject()).getObject());
  }

  @CronapiMetaData(type = "function", name = "{{datasourceInsert}}", nameTags = { "insert", "create", "novo", "inserir",
      "criar" }, description = "{{functionToInsertObjectInDatasource}}", params = { "{{datasource}}",
          "{{params}}" }, paramsType = { ObjectType.DATASET,
              ObjectType.LIST }, returnType = ObjectType.VOID, arbitraryParams = true, wizard = "procedures_sql_insert_callnoreturn")
  public static void insert(Var entity, Var ... params) {
    DataSource ds = new DataSource(entity.getObjectAsString());
    ds.insert();
    ds.updateFields(params);
    ds.save();
    ds.flush();
  }

  public static void insert(Var entity, Var object) {
    if (!object.equals(Var.VAR_NULL)) {
      DataSource ds = new DataSource(entity.getObjectAsString());
      ds.insert(object.getObjectAsMap());
      Object saved = ds.save();
      ds.flush();
      object.updateWith(saved);
    }
  }

  @CronapiMetaData(type = "function", name = "{{update}}", nameTags = { "update", "edit", "editar",
      "alterar" }, description = "{{functionToUpdateObjectInDatasource}}", params = { "{{datasource}}",
          "{{entity}}" }, paramsType = { ObjectType.DATASET,
              ObjectType.OBJECT }, returnType = ObjectType.VOID, arbitraryParams = true, wizard = "procedures_sql_update_callnoreturn")
  public static void update(Var entity, Var object) {
    if (!object.equals(Var.VAR_NULL)) {
      DataSource ds = new DataSource(entity.getObjectAsString());
      ds.filter(object, null);
      ds.update(new Var(object.getObjectAsMap()));
      Object saved = ds.save();
      object.updateWith(saved);
    }
  }

  @CronapiMetaData(type = "function", name = "{{datasourceRemove}}", nameTags = { "remove", "delete", "remover",
      "deletar", "excluir" }, description = "{{functionToRemoveObject}}", params = { "{{datasource}}",
          "{{entity}}" }, paramsType = { ObjectType.DATASET,
              ObjectType.OBJECT }, returnType = ObjectType.VOID, arbitraryParams = true, wizard = "procedures_sql_delete_callnoreturn")
  public static void remove(Var entity, Var object) {
    if (!object.equals(Var.VAR_NULL)) {
      DataSource ds = new DataSource(entity.getObjectAsString());
      ds.filter(object, null);
      ds.delete();
    }
  }

  @CronapiMetaData(type = "function", name = "{{datasourceGetField}}", nameTags = { "getField",
      "obterCampo" }, description = "{{functionToGetFieldOfCurrentCursorInDatasource}}", params = { "{{datasource}}",
          "{{fieldName}}" }, paramsType = { ObjectType.DATASET,
              ObjectType.STRING }, returnType = ObjectType.OBJECT, wizard = "procedures_get_field")
  public static Var getField(@ParamMetaData(blockType = "variables_get", type = ObjectType.OBJECT, description = "{{datasource}}") Var ds,
                             @ParamMetaData(blockType = "procedures_get_field_datasource", type = ObjectType.STRING, description = "{{fieldName}}") Var fieldName) {
    return ds.getField(fieldName.getObjectAsString());
  }

  @CronapiMetaData(type = "function", name = "{{datasourceGetField}}", nameTags = { "getField",
      "obterCampo" }, description = "{{functionToGetFieldOfCurrentCursorInDatasource}}", returnType = ObjectType.STRING, wizard = "procedures_get_field_datasource")
  public static Var getFieldFromDatasource() {
    return Var.VAR_NULL;
  }

  @CronapiMetaData(type = "function", name = "{{datasourceRemove}}", nameTags = { "remove", "delete", "apagar",
      "remover" }, description = "{{functionToRemoveObjectInDatasource}}", params = {
          "{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.VOID, displayInline = true)
  public static void remove(Var ds) {
    ((DataSource)ds.getObject()).delete();
  }

  @CronapiMetaData(type = "function", name = "{{datasourceExecuteQuery}}", nameTags = { "datasourceExecuteQuery",
      "executeCommand", "executarComando" }, description = "{{functionToExecuteQuery}}", params = { "{{entity}}",
          "{{query}}", "{{paramsQueryTuples}}" }, paramsType = { ObjectType.STRING, ObjectType.STRING,
              ObjectType.LIST }, returnType = ObjectType.DATASET, arbitraryParams = true, wizard = "procedures_sql_command_callnoreturn")
  public static void execute(Var entity, Var query, Var ... params) {
    DataSource ds = new DataSource(entity.getObjectAsString());
    ds.execute(query.getObjectAsString(), params);
  }

  @CronapiMetaData(type = "function", name = "{{newEntity}}", nameTags = { "newEntity",
      "NovaEntidade" }, description = "{{newEntityDescription}}", params = { "{{entity}}",
          "{{params}}" }, paramsType = { ObjectType.STRING,
              ObjectType.MAP }, returnType = ObjectType.OBJECT, arbitraryParams = true, wizard = "procedures_createnewobject_callreturn")
  public static final Var newEntity(Var object, Var ... params) throws Exception {
    return cronapi.object.Operations.newObject(object, params);
  }

  @CronapiMetaData(type = "function", name = "{{datasourceExecuteJQPLQuery}}", nameTags = { "datasourceQuery",
      "openConnection", "abrirConsulta" }, description = "{{functionToQueryInDatasource}}", params = {
          "{{entity}}", "{{query}}", "{{paramsQueryTuples}}" }, paramsType = { ObjectType.STRING,
              ObjectType.STRING,
              ObjectType.MAP }, returnType = ObjectType.DATASET)
  public static Var executeQuery(Var entity, Var query, Var params) {
    Map<String, Var> map = params.getObjectAsMap();
    Var[] vars = new Var[map.size()];

    int i = 0;
    for (Map.Entry<String, Var> entry : map.entrySet()) {
      vars[i] = new Var(entry.getKey(), entry.getValue());
      i++;
    }
    return query(entity, query, vars);
  }

  	@CronapiMetaData(type = "function", name = "{{datasourceGetColumnName}}", nameTags = { "GetColumn",
			"obterColuna","datasource","dados" }, description = "{{datasourceGetColumnDescription}}", params = {
					"{{datasource}}", "{{fieldName}}" }, paramsType = { ObjectType.DATASET,
							ObjectType.STRING }, returnType = ObjectType.OBJECT, wizard = "procedures_get_field")
	public static Var getColumn(
			@ParamMetaData(blockType = "variables_get", type = ObjectType.OBJECT, description = "{{datasource}}") Var ds,
			@ParamMetaData(blockType = "procedures_get_field_datasource", type = ObjectType.STRING, description = "{{fieldName}}") Var fieldName) {
		Object obj = ds.getObject();

		List<Object> dst = new LinkedList<>();
		if (obj instanceof DataSource) {
			DataSource datasource = (DataSource) obj;

			while (datasource.hasNext()) {
				dst.add(ds.getField(fieldName.getObjectAsString()));
				datasource.next();
			}
			dst.add(ds.getField(fieldName.getObjectAsString()));
			datasource.setCurrent(0);
			return Var.valueOf(dst);
		}
		return Var.valueOf(dst);
	}

  @CronapiMetaData(type = "function", name = "{{commitTransaction}}", nameTags = {"commitTransaction",
      "commitTransação"}, description = "{{commitTransactionDescription}}", params = {"{{entity}}"}, paramsType = {
      ObjectType.STRING}, returnType = ObjectType.VOID, wizard = "procedures_setentity_callnoreturn")
  public static final void commitTransaction(Var object) throws Exception {
    if (!object.equals(Var.VAR_NULL)) {
      String className = object.getObjectAsString();
      Class<?> c = Class.forName(className);
      TransactionManager.commit(c);
    }
  }

  @CronapiMetaData(type = "function", name = "{{rollbackTransaction}}", nameTags = {"rollbackTransaction",
      "rollbackTransação"}, description = "{{rollbackTransactionDescription}}", params = {"{{entity}}"}, paramsType = {
      ObjectType.STRING }, returnType = ObjectType.VOID, wizard = "procedures_setentity_callnoreturn")
  public static final void rollbackTransaction(Var object) throws Exception {
    if (!object.equals(Var.VAR_NULL)) {
      String className = object.getObjectAsString();
      Class<?> c = Class.forName(className);
      TransactionManager.rollback(c);
    }
  }

    @CronapiMetaData(type = "function", name = "{{flushTransaction}}", nameTags = {"flushTransaction",
        "flushTransação"}, description = "{{flushTransactionDescription}}", params = {"{{entity}}"}, paramsType = {
        ObjectType.STRING}, returnType = ObjectType.VOID, wizard = "procedures_setentity_callnoreturn")
    public static final void flushTransaction(Var object) throws Exception {
      if (!object.equals(Var.VAR_NULL)) {
        String className = object.getObjectAsString();
        Class<?> c = Class.forName(className);
        TransactionManager.flush(c);
      }
    }

  @CronapiMetaData(type = "function", name = "{{beginTransaction}}", nameTags = {"beginTransaction",
      "iniciarTransação"}, description = "{{beginTransaction}}", params = {"{{entity}}"}, paramsType = {
      ObjectType.STRING}, returnType = ObjectType.VOID, wizard = "procedures_setentity_callnoreturn")
  public static final void beginTransaction(Var object) throws Exception {
    if (!object.equals(Var.VAR_NULL)) {
      String className = object.getObjectAsString();
      Class<?> c = Class.forName(className);
      TransactionManager.begin(c);
    }
  }

  @CronapiMetaData(name = "{{datasourceExecuteNativeQuery}}", nameTags = { "datasourceExecuteNativeQuery",
        "executeNativeQuery", "executarQueryNativa" }, description = "{{functionToExecuteNativeQuery}}", params = {
        "{{entity}}", "{{query}}", "{{paramsQueryTuples}}" }, paramsType = { ObjectType.STRING, ObjectType.STRING,
        ObjectType.LIST }, returnType = ObjectType.DATASET, arbitraryParams = true, wizard = "procedures_sql_command_callreturn")
  public static Var executeNativeQuery(Var entity, Var query, Var... params) throws Exception {
    Query nativeQuery = sanitizeNativeQuery(entity, query, Var.valueOf(false), params);
    return Var.valueOf(nativeQuery.getResultList());
  }

  @CronapiMetaData(name = "{{datasourceExecuteNativeQueryUpdate}}", nameTags = { "datasourceExecuteNativeQueryUpdate",
         "executeNativeQueryUpdate", "executarQueryNativaAlteracao" }, description = "{{functionToExecuteNativeQueryUpdate}}",
         params = { "{{entity}}", "{{query}}", "{{paramsQueryTuples}}" }, paramsType = { ObjectType.STRING, ObjectType.STRING,
         ObjectType.LIST }, returnType = ObjectType.LONG, arbitraryParams = true, wizard = "procedures_sql_command_callreturn")
  public static Var executeNativeQueryUpdate(Var entity, Var query, Var... params) throws Exception {
    Query nativeQuery = sanitizeNativeQuery(entity, query, Var.valueOf(true), params);
    return Var.valueOf(nativeQuery.executeUpdate());
  }

  private static Query createNativeQuery(Var entity, Var isModififyQuery, String query) throws Exception {
    String namespace = entity.getObjectAsString().split("\\.")[0];
    Class<?> domainClass = Class.forName(entity.getObjectAsString());
    if (!isModififyQuery.getObjectAsBoolean()) {
      EntityManagerFactory factory = Persistence.createEntityManagerFactory(namespace);
      EntityManager entityManager = factory.createEntityManager();
      return entityManager.createNativeQuery(query, domainClass);
    }
    else {
      //Obtendo o EntityManager do TransactionManager para obter a transação corrente
      EntityManager entityManager = TransactionManager.getEntityManager(domainClass);
      return entityManager.createNativeQuery(query);
    }
  }

  private static Query sanitizeNativeQuery(Var entity, Var query, Var isModififyQuery, Var... params) throws Exception {
    String replacement = "?";
    String parameterizedQuery = query.getObjectAsString();

    List<String> parsedParams = JPQLParserUtil.parseParams(parameterizedQuery);

    for (String param : parsedParams) {
      parameterizedQuery = parameterizedQuery.replaceFirst(":" + param, replacement);
    }

    Query nativeQuery = createNativeQuery(entity, isModififyQuery, parameterizedQuery);

    if (params != null && params.length > 0) {
      Map<String, Object> paramsValues = new LinkedHashMap<>();

      for (Var param : params) {
        paramsValues.put(param.getId(), param.getObject());
      }

      int position = 1;
      for (String param : parsedParams) {
        nativeQuery.setParameter(position, paramsValues.get(param));
        position++;
      }
    }

    return nativeQuery;
  }

    @CronapiMetaData(type = "function", name = "{{executeProcedureReturnTable}}", nameTags = {"execute",
            "procedure"}, description = "{{executeProcedureReturnTableDescription}}", params = {"{{namespace}}", "{{procedureName}}", "{{parameterList}}",
            "{{returnClass}}"}, paramsType = {ObjectType.STRING, ObjectType.STRING,
            ObjectType.LIST, ObjectType.STRING}, returnType = ObjectType.LIST)
    public static Var executeProcedure(Var namespace,
                                       Var storeProcedure,
                                       Var param,
                                       Var nameClass) throws Exception {

        EntityManagerFactory factory = Persistence.createEntityManagerFactory(namespace.getObjectAsString());
        EntityManager em = factory.createEntityManager();

        List<Var> values = param.getObjectAsList();
        List<String> args = new ArrayList<>();
        values.forEach(c -> args.add("?"));
        String strExecute = String.format("{call %s(%s)}", storeProcedure.getObjectAsString(), String.join(",", args));

        Query query = em.createNativeQuery(strExecute);

        for (int i = 0; i < values.size(); i++) {
            query.setParameter(i + 1, values.get(i).getObjectAsString());
        }

        List<Object[]> result = query.getResultList();
        List<Object> returnValues = new ArrayList<>();

        // try parse
        if (!nameClass.isEmptyOrNull()) {

            try {

                Class<?> clazz = Class.forName(nameClass.getObjectAsString());
                Field[] fieldArray = clazz.getDeclaredFields();

                for (Object value : result) {
                    Object instance = clazz.newInstance();
                    if (!(value instanceof Object[])) {
                        fieldArray[0].setAccessible(true);
                        fieldArray[0].set(instance, value);
                    } else {
                        for (int i = 0; i < fieldArray.length; i++) {
                            fieldArray[i].setAccessible(true);
                            fieldArray[i].set(instance, ((Object[]) value)[i]);
                        }
                    }
                    returnValues.add(instance);
                }
                return Var.valueOf(returnValues);

            } catch (Exception e) {
            }
        }

        return Var.valueOf(result);
    }


    @CronapiMetaData(type = "function", name = "{{executeProcedureWithInOut}}", nameTags = {"execute",
            "procedure"}, description = "{{executeProcedureWithInOutDescription}}", params = {"{{namespace}}", "{{procedureName}}", "{{parameterMap}}",
            "{{paramteterListTypes}}", "{{paramteterListModes}}"}, paramsType = {ObjectType.STRING, ObjectType.STRING,
            ObjectType.MAP, ObjectType.LIST, ObjectType.LIST}, returnType = ObjectType.MAP)
    public static Var executeProcedureInOut(Var namespace,
                                            Var storeProcedure,
                                            Var param,
                                            Var paramModes,
                                            Var paramTypes
    ) throws Exception {

        Map mapReturn = new LinkedHashMap<>();

        Map<String, ParameterMode> paramModeMap = new HashMap<>();
        paramModeMap.put("IN", ParameterMode.IN);
        paramModeMap.put("OUT", ParameterMode.OUT);
        paramModeMap.put("INOUT", ParameterMode.INOUT);
        paramModeMap.put("CURSOR", ParameterMode.REF_CURSOR);

        // prepare entity manager
        EntityManagerFactory factory = Persistence.createEntityManagerFactory(namespace.getObjectAsString());
        EntityManager em = factory.createEntityManager();
        StoredProcedureQuery sp = em.createStoredProcedureQuery(storeProcedure.getObjectAsString());

        // params
        List keys = new ArrayList(param.getObjectAsMap().keySet());
        List<Var> modes = paramModes.getObjectAsList();

        // types is not null
        List<Var> types = null;
        if (paramTypes != null && !paramTypes.isEmptyOrNull()) types = paramTypes.getObjectAsList();

        // set class name default
        String nameClass = "java.lang.String";
        Class<?> clazz = Class.forName(nameClass);

        // has ref cursor
        int posCursor = modes.indexOf(Var.valueOf("CURSOR"));
        Boolean hasOut = modes.indexOf(Var.valueOf("OUT")) > -1 || modes.indexOf(Var.valueOf("INOUT")) > -1;
        Boolean hasCursor = posCursor > -1;

        for (int i = 0; i < keys.size(); i++) {

            // get params
            String paramName = keys.get(i).toString();
            String paramModeName = modes.get(i).getObjectAsString();
            ParameterMode paramMode = paramModeMap.get(paramModeName);

            // get class name by param
            if (types != null) {
                nameClass = types.get(i).getObjectAsString();
            } else {
                try {
                    // force get class name
                    nameClass = param.get(paramName).getClass().getName();
                } catch (Exception e) {
                }
            }

            clazz = Class.forName(nameClass);
            sp.registerStoredProcedureParameter(paramName, clazz, paramMode);

            // set parameters IN and INOUT
            if ("IN".equals(paramModeName) || "INOUT".equals(paramModeName))
                sp.setParameter(paramName, param.get(paramName));
        }

        // execute sp
        sp.execute();

        if (hasCursor || !hasOut) {

            // get result ref cursor
            List<Object[]> result = sp.getResultList();
            String key = hasOut ? keys.get(posCursor).toString() : "result";
            mapReturn.put(key, result);

        } else {

            // get parameters out
            for (int i = 0; i < keys.size(); i++) {
                //get params
                String paramName = keys.get(i).toString();
                String paramModeName = modes.get(i).getObjectAsString();

                // get parameters OUT and INOUT
                if ("OUT".equals(paramModeName) || "INOUT".equals(paramModeName)) {
                    Object value = sp.getOutputParameterValue(paramName);
                    mapReturn.put(paramName, value);
                }
            }
        }

        return Var.valueOf(mapReturn);
    }

}