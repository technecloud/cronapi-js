package cronapi.database;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.Var;

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
  
  @CronapiMetaData(type = "function", name = "{{datasourceQuery}}", nameTags = { "datasourceQuery", "openConnection",
      "abrirConsulta" }, description = "{{functionToQueryInDatasource}}", params = { "{{entity}}", "{{query}}",
          "{{paramsQueryTuples}}" }, paramsType = { ObjectType.STRING, ObjectType.STRING,
              ObjectType.LIST }, returnType = ObjectType.DATASET, arbitraryParams = true, wizard = "procedures_sql_callreturn")
  public static Var query(Var entity, Var query, Var ... params) {
    DataSource ds = new DataSource(entity.getObjectAsString());
    if(query == Var.VAR_NULL)
      ds.fetch();
    else {
      ds.filter(query.getObjectAsString(), params);
    }
    Var varDs = new Var(ds);
    return varDs;
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
    return new Var(((DataSource)ds.getObject()).getObject() != null);
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
  }
  
  public static void insert(Var entity, Var object) {
    if(!object.equals(Var.VAR_NULL)) {
      DataSource ds = new DataSource(entity.getObjectAsString());
      ds.insert(object.getObjectAsMap());
      Object saved = ds.save();
      object.updateWith(saved);
    }
  }
  
  @CronapiMetaData(type = "function", name = "{{update}}", nameTags = { "update", "edit", "editar",
      "alterar" }, description = "{{functionToUpdateObjectInDatasource}}", params = { "{{datasource}}",
          "{{entity}}" }, paramsType = { ObjectType.DATASET,
              ObjectType.OBJECT }, returnType = ObjectType.VOID, arbitraryParams = true, wizard = "procedures_sql_update_callnoreturn")
  public static void update(Var entity, Var object) {
    if(!object.equals(Var.VAR_NULL)) {
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
    if(!object.equals(Var.VAR_NULL)) {
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
  
}
