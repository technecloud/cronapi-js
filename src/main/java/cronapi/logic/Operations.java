package cronapi.logic;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.Var;
import cronapi.Var.Type;
import org.apache.commons.lang3.ObjectUtils;

@CronapiMetaData(category = CategoryType.LOGIC, categoryTags = {"Lógica", "Logic"})
public class Operations {

  @CronapiMetaData(type = "function", name = "{{isNullName}}", nameTags = {
      "isNullFunction"}, description = "{{isNullDescription}}", displayInline = true, returnType = ObjectType.BOOLEAN)
  public static final Var isNull(
      @ParamMetaData(type = ObjectType.OBJECT, description = "{{parameter}}") Var var) {
    return ObjectUtils.equals(var, null) ? Var.VAR_TRUE : Var.VAR_FALSE;
  }

  @CronapiMetaData(type = "function", name = "{{isNullOrEmptyName}}", nameTags = {
      "isNullOrEmptyFunction"}, description = "{{isNullOrEmptyDescription}}", displayInline = true, returnType = ObjectType.BOOLEAN)
  public static final Var isNullOrEmpty(
      @ParamMetaData(type = ObjectType.OBJECT, description = "{{parameter}}") Var var) {

    return isNull(var).equals(Var.VAR_TRUE) || isEmpty(var).equals(Var.VAR_TRUE) ? Var.VAR_TRUE
        : Var.VAR_FALSE;
  }

  @CronapiMetaData(type = "function", name = "{{isEmptyName}}", nameTags = {
      "isEmptyFunction"}, description = "{{isEmptyDescription}}", displayInline = true, returnType = ObjectType.BOOLEAN)
  public static final Var isEmpty(
      @ParamMetaData(type = ObjectType.OBJECT, description = "{{parameter}}") Var var) {
    if (!isNull(var).equals(Var.VAR_TRUE)) {
      if (var.getType().equals(Type.STRING) && var.getObjectAsString().trim().isEmpty()) {
        return Var.VAR_TRUE;

      } else if (var.getType().equals(Var.Type.LIST) && var.getObjectAsList().isEmpty()) {
        return Var.VAR_TRUE;
      } else {
        return Var.VAR_FALSE;
      }
    }
    return Var.VAR_FALSE;
  }

}