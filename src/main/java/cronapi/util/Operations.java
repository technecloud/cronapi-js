package cronapi.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import cronapi.rest.security.BlocklySecurity;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import cronapi.ClientCommand;
import cronapi.CronapiMetaData;
import cronapi.ParamMetaData;
import cronapi.RestClient;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.clazz.CronapiClassLoader;
import cronapi.i18n.Messages;

@CronapiMetaData(category = CategoryType.UTIL, categoryTags = { "Util" })
public class Operations {

	public static boolean IS_DEBUG;

	public static boolean IS_WINDOWS;
	public static boolean IS_LINUX;

	static {
		String SO = System.getProperty("os.name");
		if (SO.indexOf(' ') > -1)
			SO = SO.substring(0, SO.indexOf(' '));

		IS_WINDOWS = SO.equalsIgnoreCase("Windows");
		IS_LINUX = SO.equalsIgnoreCase("Linux");

		IS_DEBUG = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
	}

	@CronapiMetaData(type = "function", name = "{{copyTextToTransferAreaName}}", nameTags = {
			"copyTextToTransferArea" }, description = "{{copyTextToTransferAreaDescription}}", params = {
					"{{copyTextToTransferAreaParam0}}" }, paramsType = { ObjectType.STRING })
	public static final void copyTextToTransferArea(Var strVar) throws Exception {
		String str = strVar.getObjectAsString();
		java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
		java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(str);
		clipboard.setContents(selection, null);
	}

	@CronapiMetaData(type = "function", name = "{{shellExecuteName}}", nameTags = {
			"shellExecute" }, description = "{{shellExecuteDescription}}", params = { "{{shellExecuteParam0}}",
					"{{shellExecuteParam1}}" }, paramsType = { ObjectType.STRING,
							ObjectType.BOOLEAN }, returnType = ObjectType.STRING)
	public static final Var shellExecute(Var cmdline, Var waitFor) throws Exception {
		Boolean waitForCasted = (Boolean) waitFor.getObject();
		Process p = Runtime.getRuntime().exec(cmdline.getObjectAsString());
		if (waitForCasted) {
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String r = "";
			String line;
			while ((line = input.readLine()) != null) {
				r += (line + "\n");
			}
			input.close();
			return new Var(r);
		}
		return new Var();
	}

	// Retorna um numério aleatório
	@CronapiMetaData(type = "function", name = "{{randomName}}", nameTags = {
			"random" }, description = "{{randomDescription}}", params = {
					"{{randomParam0}}" }, paramsType = { ObjectType.DOUBLE }, returnType = ObjectType.DOUBLE)
	public static final Var random(Var maxValue) throws Exception {
		return new Var(Math.round(Math.random() * maxValue.getObjectAsDouble()));
	}

	@CronapiMetaData(type = "function", name = "{{compressToZipName}}", nameTags = {
			"compressToZip" }, description = "{{compressToZipDescription}}", params = {
					"{{compressToZipParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var compressToZip(Var value) throws Exception {
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		java.util.zip.DeflaterOutputStream compresser = new java.util.zip.DeflaterOutputStream(output);
		compresser.write((byte[]) value.getObject());
		compresser.finish();
		compresser.close();
		return new Var(output.toByteArray());
	}

	@CronapiMetaData(type = "function", name = "{{decodeZipFromByteName}}", nameTags = {
			"decodeZipFromByte" }, description = "{{decodeZipFromByteDescription}}", params = {
					"{{decodeZipFromByteParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var decodeZipFromByte(Var value) throws Exception {
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream((byte[]) value.getObject());
		java.util.zip.InflaterInputStream decompresser = new java.util.zip.InflaterInputStream(input);
		byte[] buffer = new byte[1024 * 4];//4KB
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		int len;
		while ((len = decompresser.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		decompresser.close();
		out.close();
		input.close();
		return new Var(out.toByteArray());
	}

	@CronapiMetaData(type = "function", name = "{{sleep}}", nameTags = {
			"sleep" }, description = "{{functionToSleep}}", params = {
					"{{timeSleepInSecond}}" }, paramsType = { ObjectType.LONG }, returnType = ObjectType.VOID)
	public static final void sleep(Var time) throws Exception {
		long sleepTime = (time.getObjectAsInt() * 1000);
		Thread.sleep(sleepTime);
	}

	@CronapiMetaData(type = "function", name = "{{throwException}}", nameTags = {
			"throwException" }, description = "{{functionToThrowException}}", params = {
					"{{exceptionToBeThrow}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.VOID)
	public static final void throwException(Var exception) throws Exception {
		if (exception.getObject() instanceof Exception)
			throw Exception.class.cast(exception.getObject());
		else if (exception.getObject() instanceof String)
			throw new Exception(exception.getObjectAsString());
	}

	@CronapiMetaData(type = "function", name = "{{createExceptionName}}", nameTags = {
			"createException" }, description = "{{createExceptionName}}", params = {
					"{{createExceptionParam0}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public static final Var createException(Var msg) throws Exception {
		Exception e = new Exception(msg.getObjectAsString());
		return new Var(e);
	}

	@CronapiMetaData(type = "function", name = "{{callBlocklyNoReturnName}}", nameTags = {
			"callBlocklyNoReturn" }, description = "{{callBlocklyNoReturnDescription}}", wizard = "procedures_callblockly_callnoreturn", returnType = ObjectType.VOID, arbitraryParams = true)
	public static final void callBlocklyNoReturn(
			@ParamMetaData(type = ObjectType.STRING, description = "{{callBlocklyNoReturnParam0}}") Var classNameWithMethod,
			@ParamMetaData(type = ObjectType.STRING, description = "{{callBlocklyNoReturnParam1}}") Var... params)
			throws Exception {
		callBlockly(classNameWithMethod, params);
	}

	@CronapiMetaData(type = "function", name = "{{callClienteFunctionName}}", nameTags = {
			"callClienteFunction" }, description = "{{callClienteFunctionDescription}}", returnType = ObjectType.VOID, arbitraryParams = true)
	public static final void callClientFunction(
			@ParamMetaData(type = ObjectType.STRING, description = "{{callClienteFunctionParam0}}") Var function,
			@ParamMetaData(type = ObjectType.STRING, description = "{{callClienteFunctionParam1}}") Var... params)
			throws Exception {
		ClientCommand command = new ClientCommand(function.getObjectAsString());
		for (Var p : params)
			command.addParam(p);

		RestClient.getRestClient().addCommand(command);
	}

  @CronapiMetaData(type = "function", name = "{{callBlockly}}", nameTags = {
      "callBlockly" }, description = "{{functionToCallBlockly}}", params = { "{{classNameWithMethod}}",
      "{{params}}" }, wizard = "procedures_callblockly_callreturn", paramsType = { ObjectType.OBJECT,
      ObjectType.OBJECT }, returnType = ObjectType.OBJECT, arbitraryParams = true)
  public static final Var callBlockly(Var classNameWithMethod, Var... params) throws Exception {
	  return callBlockly(classNameWithMethod, false, "", params);
  }

  @CronapiMetaData(type = "internal")
	public static final Var callBlockly(Var classNameWithMethod, boolean checkSecurity, String restMethod, Var... params) throws Exception {

		String className = classNameWithMethod.getObjectAsString();
		String method = null;
		if (className.indexOf(":") > -1) {
			method = className.substring(className.indexOf(":") + 1);
			className = className.substring(0, className.indexOf(":"));
		}

		final Class clazz;

		if (IS_DEBUG) {
			CronapiClassLoader loader = new CronapiClassLoader();
			clazz = loader.findClass(className);
		} else {
			clazz = Class.forName(className);
		}

		if (checkSecurity) {
      BlocklySecurity.checkSecurity(clazz, restMethod);
    }

		Method methodToCall = clazz.getMethods()[0];
		for (Method m : clazz.getMethods()) {
			if (m.getName().equalsIgnoreCase(method)) {
				methodToCall = m;
				break;
			}
		}

    if (params == null)
      params = new Var[0];

		Var[] callParams = params;

		if (methodToCall.getParameterCount() != callParams.length) {
			callParams = new Var[methodToCall.getParameterCount()];
			for (int i = 0; i < methodToCall.getParameterCount(); i++) {
				if (i < params.length)
					callParams[i] = params[i];
				else
					callParams[i] = Var.VAR_NULL;
			}
		}

		boolean isBlockly = false;
		for (Annotation annotation : clazz.getAnnotations()) {
			if (annotation.annotationType().getName().equals("cronapi.CronapiMetaData")) {
				Method type = annotation.annotationType().getMethod("type");
				if (type != null) {
					String value = (String) type.invoke(annotation);
					if (value != null && value.equals("blockly")) {
						isBlockly = true;
					}
				}
			}
		}
		if (!isBlockly) {
			throw new Exception(Messages.getString("accessDenied"));
		}
		Object o = methodToCall.invoke(clazz, callParams);
		return Var.valueOf(o);
	}

	@CronapiMetaData(type = "function", name = "{{encryptPasswordName}}", nameTags = {
			"encryptPassword" }, description = "{{encryptPasswordDescription}}", params = {
					"{{encryptPasswordParam0}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	public static final Var encryptPassword(Var password) throws Exception {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return new Var(passwordEncoder.encode(password.getObjectAsString()));
	}

	@CronapiMetaData(type = "function", name = "{{matchesencryptPasswordName}}", nameTags = {
			"matchesencryptPassword" }, description = "{{matchesencryptPasswordDescription}}", params = {
					"{{matchesencryptPasswordParam0}}", "{{matchesencryptPasswordParam1}}" }, paramsType = {
							ObjectType.STRING, ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var matchesencryptPassword(Var password, Var encrypted) throws Exception {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return new Var(passwordEncoder.matches(password.getObjectAsString(), encrypted.getObjectAsString()));
	}

	@CronapiMetaData(type = "function", name = "{{getURLFromOthersName}}", nameTags = {
			"matchesencryptPassword" }, description = "{{getURLFromOthersDescription}}", returnType = ObjectType.STRING)
	public static final Var getURLFromOthers(
			@ParamMetaData(type = ObjectType.STRING, description = "{{HTTPMethod}}", blockType = "util_dropdown", keys = {
					"GET", "POST", "PUT",
					"DELETE" }, values = { "{{HTTPGet}}", "{{HTTPPost}}", "{{HTTPPut}}", "{{HTTPDelete}}" }) Var method,
			@ParamMetaData(type = ObjectType.STRING, description = "{{contentType}}", blockType = "util_dropdown", keys = {
					"application/x-www-form-urlencoded",
					"application/json" }, values = { "{{x_www_form_urlencoded}}", "{{app_json}}" }) Var contentType,
			@ParamMetaData(type = ObjectType.STRING, description = "{{URLAddress}}") Var address,
			@ParamMetaData(type = ObjectType.MAP, description = "{{paramsHTTP}}") Var params,
			@ParamMetaData(type = ObjectType.MAP, description = "{{cookieContainer}}") Var cookieContainer)
			throws Exception {
		try {

			String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
			String APPLICATION_JSON = "application/json";

			if (method.getObjectAsString().toUpperCase().equals("GET")) {

				HttpClient httpClient = HttpClients.createDefault();
				HttpGet httpGet = new HttpGet(address.getObjectAsString());

				LinkedHashMap<Var, Var> headerObject = (LinkedHashMap<Var, Var>) cookieContainer.getObjectAsMap();
				headerObject.entrySet().stream().forEach((entry) -> {
					httpGet.addHeader(entry.getKey().getObjectAsString(),
							new Var(entry.getValue()).getObjectAsString());
				});

				HttpResponse httpResponse = httpClient.execute(httpGet);
				Scanner scanner = new Scanner(httpResponse.getEntity().getContent(),
						cronapi.CronapiConfigurator.ENCODING);
				String response = "";
				try {
					response = scanner.useDelimiter("\\A").next();
				} catch (Exception e) {
				}
				scanner.close();
				httpGet.completed();
				return new Var(response);
			} else if (method.getObjectAsString().toUpperCase().equals("POST")) {
				HttpClient httpClient = HttpClients.createDefault();
				HttpPost httpPost = new HttpPost(address.getObjectAsString());
				LinkedHashMap<Var, Var> headerObject = (LinkedHashMap<Var, Var>) cookieContainer.getObjectAsMap();
				headerObject.entrySet().stream().forEach((entry) -> {
					httpPost.addHeader(entry.getKey().getObjectAsString(),
							new Var(entry.getValue()).getObjectAsString());
				});

				if (params != Var.VAR_NULL) {

					if (APPLICATION_X_WWW_FORM_URLENCODED.equals(contentType.getObjectAsString().toLowerCase())) {

						LinkedHashMap<Var, Var> mapObject = (LinkedHashMap<Var, Var>) params.getObjectAsMap();
						List<NameValuePair> params2 = new LinkedList<>();
						mapObject.entrySet().stream().forEach((entry) -> {
							params2.add(new BasicNameValuePair( new Var(entry.getKey()).getObjectAsString(),
									new Var(entry.getValue()).getObjectAsString()));
						});

						httpPost.setEntity(new UrlEncodedFormEntity(params2, cronapi.CronapiConfigurator.ENCODING));
					} else if (APPLICATION_JSON.equals(contentType.getObjectAsString().toLowerCase())) {

						StringEntity params2 = new StringEntity(params.getObjectAsString(),
								Charset.forName(cronapi.CronapiConfigurator.ENCODING));
						httpPost.setEntity(params2);
					}
				}

				HttpResponse httpResponse = httpClient.execute(httpPost);
				Scanner scanner = new Scanner(httpResponse.getEntity().getContent(),
						cronapi.CronapiConfigurator.ENCODING);
				String response = "";
				try {
					response = scanner.useDelimiter("\\A").next();
				} catch (Exception e) {
				}
				scanner.close();
				return new Var(response);

			} else if (method.getObjectAsString().toUpperCase().equals("PUT")) {
				HttpClient httpClient = HttpClients.createDefault();
				HttpPut httpPut = new HttpPut(address.getObjectAsString());

				LinkedHashMap<Var, Var> headerObject = (LinkedHashMap<Var, Var>) cookieContainer.getObjectAsMap();
				headerObject.entrySet().stream().forEach((entry) -> {
					httpPut.addHeader(entry.getKey().getObjectAsString(),
							new Var(entry.getValue()).getObjectAsString());
				});

				if (params != Var.VAR_NULL) {
					if (APPLICATION_X_WWW_FORM_URLENCODED.equals(contentType.getObjectAsString().toLowerCase())) {
						LinkedHashMap<Var, Var> mapObject = (LinkedHashMap<Var, Var>) params.getObjectAsMap();
						List<NameValuePair> params2 = new LinkedList<>();
						mapObject.entrySet().stream().forEach((entry) -> {
							params2.add(new BasicNameValuePair(entry.getKey().getObjectAsString(),
									new Var(entry.getValue()).getObjectAsString()));
						});
						httpPut.setEntity(new UrlEncodedFormEntity(params2, cronapi.CronapiConfigurator.ENCODING));
					} else if (APPLICATION_JSON.equals(contentType.getObjectAsString().toLowerCase())) {
						StringEntity params2 = new StringEntity(params.getObjectAsString(),
								Charset.forName(cronapi.CronapiConfigurator.ENCODING));
						httpPut.setEntity(params2);
					}
				}

				HttpResponse httpResponse = httpClient.execute(httpPut);
				Scanner scanner = new Scanner(httpResponse.getEntity().getContent(),
						cronapi.CronapiConfigurator.ENCODING);
				String response = "";
				try {
					response = scanner.useDelimiter("\\A").next();
				} catch (Exception e) {
				}
				scanner.close();
				return new Var(response);

			} else if (method.getObjectAsString().toUpperCase().equals("DELETE")) {
				HttpClient httpClient = HttpClients.createDefault();
				HttpDelete httpDelete = new HttpDelete(address.getObjectAsString());

				LinkedHashMap<Var, Var> headerObject = (LinkedHashMap<Var, Var>) cookieContainer.getObjectAsMap();
				headerObject.entrySet().stream().forEach((entry) -> {
					httpDelete.addHeader(entry.getKey().getObjectAsString(),
							new Var(entry.getValue()).getObjectAsString());
				});

				HttpResponse httpResponse = httpClient.execute(httpDelete);
				Scanner scanner = new Scanner(httpResponse.getEntity().getContent(),
						cronapi.CronapiConfigurator.ENCODING);
				String response = "";
				try {
					response = scanner.useDelimiter("\\A").next();
				} catch (Exception e) {
				}
				scanner.close();
				return new Var(response);
			}
			return new Var();
		} catch (Exception e) {
			throw e;
		}
	}

	// Internal Function - Missing translation
	// @CronapiMetaData(type = "function", name = "{{readLinesFromStreamName}}", nameTags = {
	//   "readLinesFromStream" }, description = "{{readLinesFromStreamDescription}}", params = {
	//   "{{readLinesFromStreamParam0}}", "{{readLinesFromStreamParam1}}" }, paramsType = { ObjectType.OBJECT, ObjectType.STATEMENTSENDER })
	public static void readLinesFromStream(Var input, Callback callback) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) input.getObject()));
		String line;
		while ((line = reader.readLine()) != null) {
			callback.call(Var.valueOf(line));
		}
	}

	// Internal Function - Missing translation
	// 	@CronapiMetaData(type = "function", name = "{{readBytesFromStreamName}}", nameTags = {
	// 			"readBytesFromStream" }, description = "{{readBytesFromStreamDescription}}", params = {
	// 					"{{readBytesFromStreamParam0}}", "{{readBytesFromStreamParam1}}",
	// 					"{{readBytesFromStreamParam2}}", }, paramsType = { ObjectType.OBJECT, ObjectType.LONG,
	// 							ObjectType.STATEMENTSENDER })
	public static final void readBytesFromStream(Var input, Var size, Callback callback) throws Exception {
		byte[] buffer = new byte[size.getObjectAsInt() > 0 ? size.getObjectAsInt() : 1024];
		InputStream ios = (InputStream) input.getObject();
		int read = 0;
		while ((read = ios.read(buffer)) != -1) {
			byte[] readBytes = Arrays.copyOf(buffer, read);
			callback.call(Var.valueOf(readBytes));
		}
	}

}