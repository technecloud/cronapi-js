/*
 * Copyright (c) 2017, Techne Engenharia e Sistemas S/C Ltda. All rights reserved.
 * TECHNE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cronapi.cloud;

import java.io.InputStream;

public final class FileObject {

	private final String fileName;

	private final InputStream fileContent;

	FileObject(String fileName, InputStream fileContent) {
		this.fileName = fileName;
		this.fileContent = fileContent;
	}

	String getFileName() {
		return fileName;
	}

	InputStream getFileContent() {
		return fileContent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		FileObject that = (FileObject) o;
		return fileName != null ? fileName.equals(that.fileName) : that.fileName == null;
	}

	@Override
	public int hashCode() {
		return fileName != null ? fileName.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "FileObject{" + "fileName='" + fileName + '\'' + '}';
	}
}
