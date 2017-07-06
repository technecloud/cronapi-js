package cronapi.object;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.Var;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-07-06
 *
 */
@CronapiMetaData(category = CategoryType.OBJECT, categoryTags = { "Object", "Objeto" })
public class Operations {
  
  @CronapiMetaData(type = "function", name = "{{getObjectFieldName}}", nameTags = {
      "getObjectFieldName" }, description = "{{getObjectFieldDescription}}", returnType = ObjectType.OBJECT, wizard = "procedures_get_field")
  public static final Var getObjectField(@ParamMetaData(blockType = "variables_get", type = ObjectType.BLOCK, description = "{{getObjectFieldParam0}}") Var objVar,
                                         @ParamMetaData(blockType = "procedures_get_field_object", type = ObjectType.BLOCK, description = "{{getObjectFieldParam1}}") Var keyVar)
          throws Exception {
    return cronapi.json.Operations.getJsonOrMapField(objVar, keyVar);
  }
  
  @CronapiMetaData(type = "function", name = "{{setObjectFieldName}}", nameTags = {
      "seObjectField" }, description = "{{setObjectFieldDescription}}", returnType = ObjectType.VOID, wizard = "procedures_set_field")
  public static final void setObjectField(@ParamMetaData(blockType = "variables_get", type = ObjectType.BLOCK, description = "{{setObjectFieldParam0}}") Var objVar,
                                          @ParamMetaData(blockType = "procedures_get_field_object", type = ObjectType.BLOCK, description = "{{setObjectFieldParam1}}") Var keyVar,
                                          @ParamMetaData(type = ObjectType.OBJECT, description = "{{setObjectFieldParam2}}") Var value)
          throws Exception {
    cronapi.json.Operations.setJsonOrMapField(objVar, keyVar, value);
  }
  
}
