package cronapi.database;

import cronapi.CronapiMetaData;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

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
	public static Var query(Var entity, Var query, Var... params) {
		DataSource ds = new DataSource(entity.getObjectAsString());
		if (query == null)
			ds.fetch();
		else {
			Object[][] paramsObject = new Object[params.length][2];
			for (int i = 0; i < params.length; i++) {
				paramsObject[i][0] = params[i].getId();
				paramsObject[i][1] = params[i].getObject();
			}
			ds.filter(query.getObjectAsString(), paramsObject);
		}
		Var varDs = new Var(ds);
		return varDs;
	}

	@CronapiMetaData(type = "function", name = "{{datasourceHasData}}", nameTags = { "hasData", "hasElements",
			"existeRegistro", "proximo", "avancar" }, description = "{{functionToMoveCursorToNextPosition}}", params = {
					"{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.VOID)
	public static void hasData(Var ds) {
		((DataSource) ds.getObject()).next();
	}

	@CronapiMetaData(type = "function", name = "{{datasourceHasNext}}", nameTags = { "hasNext", "existeProximo",
			"temProximo" }, description = "{{functionToVerifyHasNextPosition}}", params = {
					"{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.BOOLEAN)
	public static Var hasNext(Var ds) {
		return new Var(((DataSource) ds.getObject()).hasNext());
	}

	@CronapiMetaData(type = "function", name = "{{datasourceClose}}", nameTags = { "close", "fechar", "limpar",
			"clear" }, description = "{{functionToCloseAndCleanDatasource}}", params = {
					"{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.VOID)
	public static void close(Var ds) {
		((DataSource) ds.getObject()).clear();
	}

	@CronapiMetaData(type = "function", name = "{{datasourceUpdateField}}", nameTags = { "updateField",
			"atualizarCampo", "setField",
			"modificarCampo" }, description = "{{functionToUpdateFieldInDatasource}}", params = { "{{datasource}}",
					"{{fieldName}}", "{{fieldValue}}" }, paramsType = { ObjectType.DATASET, ObjectType.STRING,
							ObjectType.STRING }, returnType = ObjectType.VOID)
	public static void updateField(Var ds, Var fieldName, Var fieldValue) {
		((DataSource) ds.getObject()).updateField(fieldName.getObjectAsString(), fieldValue.getObjectAsString());
	}

	@CronapiMetaData(type = "function", name = "{{datasourceSave}}", nameTags = { "save", "flush", "salvar", "persist",
			"gravar" }, description = "{{functionToSaveCurrentObjectInDatasource}}", params = {
					"{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.VOID)
	public static void save(Var ds) {
		((DataSource) ds.getObject()).save();
	}

	@CronapiMetaData(type = "function", name = "{{datasourceInsert}}", nameTags = { "insert", "prepare", "create",
			"novo", "inserir", "criar" }, description = "{{functionToInsertObjectInDatasource}}", params = {
					"{{datasource}}", "{{paramsInsertTuples}}" }, paramsType = { ObjectType.DATASET,
							ObjectType.LIST }, returnType = ObjectType.VOID)
	public static void save(Var varDs, Var... params) {
		DataSource ds = (DataSource) varDs.getObject();
		ds.insert();
		Object[][] paramsObject = new Object[params.length][2];
		for (int i = 0; i < params.length; i++) {
			paramsObject[i][0] = params[i].getId();
			paramsObject[i][1] = params[i].getObject();
		}
		ds.updateFields(paramsObject);
		ds.save();
	}

	@CronapiMetaData(type = "function", name = "{{datasourceGetField}}", nameTags = { "getField",
			"obterCampo" }, description = "{{functionToGetFieldOfCurrentCursorInDatasource}}", params = {
					"{{datasource}}", "{{fieldName}}" }, paramsType = { ObjectType.DATASET,
							ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public static Var getField(Var ds, Var fieldName) {
		return new Var(((DataSource) ds.getObject()).getObject(fieldName.getObjectAsString()));
	}

	@CronapiMetaData(type = "function", name = "{{datasourceRemove}}", nameTags = { "remove", "delete", "apagar",
			"remover" }, description = "{{functionToRemoveObjectInDatasource}}", params = {
					"{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.VOID)
	public static void remove(Var ds) {
		((DataSource) ds.getObject()).delete();
	}
	
	@CronapiMetaData(type = "function", name = "{{datasourceRemoveConditionally}}", nameTags = { "remove", "delete", "apagar",
			"remover" }, description = "{{functionToRemoveConditionallyObjectInDatasource}}", params = {
					"{{datasource}}", "{{removeQuery}}", "{{paramsRemoveTuples}}" }, paramsType = { ObjectType.DATASET, ObjectType.STRING, ObjectType.LIST }, returnType = ObjectType.VOID)
	public static void remove(Var ds, Var removeQuery, Var...params) {
	  Object[][] paramsObject = new Object[params.length][2];
		for (int i = 0; i < params.length; i++) {
			paramsObject[i][0] = params[i].getId();
			paramsObject[i][1] = params[i].getObject();
		}
		((DataSource) ds.getObject()).delete(removeQuery.getObjectAsString(), paramsObject);
	}
	
	@CronapiMetaData(type = "function", name = "{{datasourceUpdateConditionally}}", nameTags = { "remove", "delete", "apagar",
			"remover" }, description = "{{functionToUpdateConditionallyObjectInDatasource}}", params = {
					"{{datasource}}", "{{updateQuery}}", "{{paramsUpdateTuples}}" }, paramsType = { ObjectType.DATASET, ObjectType.STRING, ObjectType.LIST }, returnType = ObjectType.VOID)
	public static void update(Var ds, Var updateQuery, Var...params) {
	  Object[][] paramsObject = new Object[params.length][2];
		for (int i = 0; i < params.length; i++) {
			paramsObject[i][0] = params[i].getId();
			paramsObject[i][1] = params[i].getObject();
		}
		((DataSource) ds.getObject()).updateFields(updateQuery.getObjectAsString(), paramsObject);
	}

}
