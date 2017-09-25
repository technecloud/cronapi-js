/*
 * Copyright (c) 2017, Techne Engenharia e Sistemas S/C Ltda. All rights reserved.
 * TECHNE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cronapi.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

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
				Object value = field.get(sourceObject);
				if (value instanceof byte[])
					fileContent = new ByteArrayInputStream((byte[]) value);
				String filePath = aClass.getSimpleName().concat("/").concat(field.getName()).concat("/");
				String identify = "";
				for (String id : ids) {
					if (!identify.isEmpty())
						identify = identify.concat("-");
					Field declaredId = aClass.getDeclaredField(id);
					declaredId.setAccessible(true);
					identify = identify.concat(id).concat("-").concat(String.valueOf(declaredId.get(sourceObject)));
				}
				files.add(new FileObject("/".concat(filePath).concat(identify), fileContent));
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			log.error(e.getMessage());
		}
		return new CloudFactory(files);
	}

}
