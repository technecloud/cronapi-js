/*
 * Copyright (c) 2017, Techne Engenharia e Sistemas S/C Ltda. All rights reserved.
 * TECHNE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cronapi.cloud;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import cronapi.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class S3Service implements CloudService {

  private static final Logger log = LoggerFactory.getLogger(S3Service.class);

  private AmazonS3 client;

  private final List<FileObject> files;
  private FieldData fieldData;

  S3Service(FieldData fieldData, List<FileObject> files) {
    this.fieldData = fieldData;
    this.files = files;
  }

  private AmazonS3 getClient() {
    if (this.client == null) {
      AWSCredentials credentials = new BasicAWSCredentials(fieldData.data.value(), fieldData.data.secret());
      AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);

      this.client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(credentialsProvider).build();
    }

    return this.client;
  }

  @Override
  public void upload() {
    if (files == null || files.isEmpty()) {
      log.warn("File content not found to Dropbox upload");
      return;
    }
    files.forEach(fileObject -> {
      try {
        PutObjectRequest request = new PutObjectRequest(fieldData.data.id(), AppConfig.guid() + fileObject.getFileName(), fileObject.getFileContent(), null);

        getClient().putObject(request);

        fileObject.setFileDirectUrl("http://" + fieldData.data.id() + "/" + AppConfig.guid() + fileObject.getFileName());

      } catch (Throwable e) {
        log.error(e.getMessage(), e);
      }
    });
  }

  @Override
  public void popule(Object target) {
	/*	this.files.forEach(fileObject -> {
			try {
				String fileName = fileObject.getFileName();
				String[] strings = fileName.split("/");
				if (strings.length > 0) {
					String fieldTarget = strings[2];
					Field declaredField = target.getClass().getDeclaredField(fieldTarget);
					declaredField.setAccessible(true);

					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					client.files().downloadBuilder(fileName).download(outputStream);

					declaredField.set(target, outputStream.toByteArray());
				}
			} catch (DbxException | IOException | NoSuchFieldException | IllegalAccessException e) {
				log.error(e.getMessage());
			}
		});*/
  }

  @Override
  public void delete() {
	/*	this.files.forEach(fileObject -> {
			try {
				client.files().delete(fileObject.getFileName());
			} catch (DbxException e) {
				log.error(e.getMessage());
			}
		});*/
  }

}
