package cronapi.json;

import java.util.HashMap;

import com.google.gson.JsonObject;

import cronapi.CronapiMetaData;
import cronapi.ParamMetaData;
import cronapi.Utils;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

 
@CronapiMetaData(category = CategoryType.JSONORMAP, categoryTags = { "Json", "Map"})
public class Operations {
  
  @CronapiMetaData(type = "function", name = "{{createObjectJson}}", nameTags = {
			"createObjectJson" }, description = "{{functionToCreateObjectJson}}", returnType = ObjectType.JSON)
	public static final Var createObjectJson() throws Exception {
    Var value = new Var(new JsonObject());
    return value;
  }
  
  @CronapiMetaData(type = "function", name = "{{createObjectMap}}", nameTags = {
			"createObjectMap" }, description = "{{functionToCreateObjectMap}}", returnType = ObjectType.OBJECT)
	public static final Var createObjectMap() throws Exception {
    Var value = new Var(new HashMap<>());
    return value;
  }


	@CronapiMetaData(type = "function", name = "{{getJsonOrMapField}}", nameTags = {
			"getJsonOrMapField" }, description = "{{functionToGetJsonOrMapField}}", returnType = ObjectType.OBJECT)
	public static final Var getJsonOrMapField(
	    @ParamMetaData(type = ObjectType.OBJECT, description = "{{mapOrJsonVar}}") Var mapVar, 
	    @ParamMetaData(type = ObjectType.STRING, description = "{{pathKey}}") Var keyVar) 
	    throws Exception {
    Var value = Var.VAR_NULL;
    Object obj = mapVar.getObject();   
    Object key = keyVar.getObject();    

    String[] path = key.toString().split("\\.");
    for (int i=0;i<path.length;i++) {    
      String k = path[i];    
      if (obj != null) {
        if (i == path.length - 1) {      
          value = Var.valueOf(Utils.mapGetObjectPathExtractElement(obj, k, false));    
        } else {
          obj = Utils.mapGetObjectPathExtractElement(obj, k, false);     
        }           
      }
    }
    return value;
  }
  
  @CronapiMetaData(type = "function", name = "{{setJsonOrMapField}}", nameTags = {
			"setJsonOrMapField" }, description = "{{functionToSetJsonOrMapField}}", returnType = ObjectType.VOID)
	public static final void setJsonOrMapField(
	        @ParamMetaData(type = ObjectType.OBJECT, description = "{{mapOrJsonVar}}") Var mapVar, 
	        @ParamMetaData(type = ObjectType.STRING, description = "{{pathKey}}") Var keyVar, 
	        @ParamMetaData(type = ObjectType.OBJECT, description = "{{valueToBetSet}}") Var value) throws Exception {
    Object obj = mapVar.getObject();   
    Object key = keyVar.getObject();    

    String[] path = key.toString().split("\\.");
    for (int i=0;i<path.length;i++) {    
      String k = path[i];    
      if (obj != null) {
        if (i == path.length - 1) {
          Utils.mapSetObject(obj, k, value);    
        } else {
          obj = Utils.mapGetObjectPathExtractElement(obj, k, true);     
        }           
      }
    }
  }
	
}
