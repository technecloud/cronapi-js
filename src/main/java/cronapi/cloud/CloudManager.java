/*
 * Copyright (c) 2017, Techne Engenharia e Sistemas S/C Ltda. All rights reserved.
 * TECHNE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cronapi.cloud;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLConnection;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CloudManager {

	private static final Logger log = LoggerFactory.getLogger(CloudManager.class);

	private Object sourceObject;
	private String[] ids;
	private String[] fieldNames;

	private CloudManager() {
	}

	public static CloudManager newInstance() {
		return new CloudManager();
	}

	public CloudManager byEntity(Object sourceObject) {
		this.sourceObject = sourceObject;
		return this;
	}

	public CloudManager byID(String... ids) {
		this.ids = ids;
		return this;
	}

	public CloudManager toFields(String... fieldNames) {
		this.fieldNames = fieldNames;
		return this;
	}

	public CloudFactory build() {
		InputStream fileContent = new ByteArrayInputStream(new byte[0]);
		Class<?> aClass = sourceObject.getClass();
		ArrayList<FileObject> files = new ArrayList<>(10);
		try {
			for (String fieldName : fieldNames) {
				Field field = aClass.getDeclaredField(fieldName);
				field.setAccessible(true);
				Object valueField = field.get(sourceObject);
				Object value = null;
				if (valueField instanceof String && isBase64Encoded((String)valueField))
				  value = java.util.Base64.getDecoder().decode(((String) valueField).getBytes("UTF-8"));
				else
				  value = valueField;

				if (value instanceof byte[]) {
					fileContent = new ByteArrayInputStream((byte[]) value);
					String fileExtension = getExtensionFromContent(fileContent);

					String filePath = aClass.getSimpleName().concat("/").concat(field.getName()).concat("/");
					String identify = "";
					for (String id : ids) {
						if (!identify.isEmpty())
							identify = identify.concat("-");
						Field declaredId = aClass.getDeclaredField(id);
						declaredId.setAccessible(true);
						identify = identify.concat(id).concat("-").concat(String.valueOf(declaredId.get(sourceObject)));
					}
					identify = identify.concat(".").concat(fileExtension);
					files.add(new FileObject("/".concat(filePath).concat(identify), fieldName, fileContent));
				}

				// field.set(sourceObject, arg1))
			}
		} catch (NoSuchFieldException | IllegalAccessException | UnsupportedEncodingException e) {
			log.error(e.getMessage());
		}
		return new CloudFactory(files);
	}

	private String getExtensionFromContent(InputStream fileContent) {
		String contentType = "image/png";
		try {
			contentType = URLConnection.guessContentTypeFromStream(fileContent);
		} catch (Exception e) {
			//Não conseguiu identificar o tipo de arquivo
		}
		String fileExtension = "png";
		if (contentType.contains("/"))
			fileExtension = contentType.split("/")[1];
		return fileExtension;
	}
	
	private boolean isBase64Encoded(String value)
    {
      if (value == null || value.isEmpty())
        return false;
      try {
          byte[] decodedString = java.util.Base64.getDecoder().decode((value).getBytes("UTF-8"));
          return (value.replace(" ","").length()  % 4 == 0);
      } catch (Exception e) {
          return false;
      }
    }

}
