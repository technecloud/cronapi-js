/*
 * Copyright (c) 2017, Techne Engenharia e Sistemas S/C Ltda. All rights reserved.
 * TECHNE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cronapi.cloud;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import java.util.List;

public final class CloudFactory {

	private final List<FileObject> files;

	CloudFactory(List<FileObject> files) {
		this.files = files;
	}

	public CloudService send(FieldData fieldData) {
	  if ("dropbox".equals(fieldData.data.type())) {
      DbxRequestConfig.Builder builder = DbxRequestConfig.newBuilder("cronapp/app");
      DbxRequestConfig requestConfig = builder.build();
      DbxClientV2 client = new DbxClientV2(requestConfig, fieldData.data.value());
      return new DropboxService(client, files);
    }

    if ("S3".equals(fieldData.data.type()) || "cloudservices".equals(fieldData.data.type()) ) {
      return new S3Service(fieldData, files);
    }

	  return null;
	}
	
	public List<FileObject> getFiles() {
	  return this.files;
	}

}
