package cronapi.pushnotification;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.http.HttpEntity;

import com.google.gson.JsonObject;

import cronapi.CronapiMetaData;
import cronapi.ParamMetaData;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2018-01-24
 *
 */
@CronapiMetaData(category = CategoryType.UTIL, categoryTags = { "UTIL", "Util" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{firebaseSendNotification}}", nameTags = {
			"SendNotification" }, description = "{{firebaseSendNotificationDescription}}" )
	public static final void sendNotification(
			@ParamMetaData(type = ObjectType.STRING, description = "{{FirebaseServerKey}}") Var serverKey,
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{FirebaseTo}}") Var paramTo,
			@ParamMetaData(type = ObjectType.STRING, description = "{{FirebaseTitle}}") Var paramTitle,
			@ParamMetaData(type = ObjectType.STRING, description = "{{FirebaseBody}}") Var paramBody,
			@ParamMetaData(type = ObjectType.JSON, description = "{{FirebaseData}}") Var paramData)
			throws Exception {

		JsonObject body = new JsonObject();
		body.addProperty("to", paramTo.getObjectAsString());
		body.addProperty("priority", "high");
		
		JsonObject notification = new JsonObject();
		notification.addProperty("title", paramTitle.getObjectAsString());
		notification.addProperty("body", paramBody.getObjectAsString());

		body.add("notification", notification);
		body.add("data", (JsonObject) paramData.getObject()); 
		
		HttpEntity<String> request = new HttpEntity<>(body.toString());
		FirebasePushNotificationService firebaseService = new FirebasePushNotificationService(serverKey.getObjectAsString());
		CompletableFuture<String> pushNotification = firebaseService.send(request);
		CompletableFuture.allOf(pushNotification).join();
		
		try {
			String firebaseResponse = pushNotification.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

}
