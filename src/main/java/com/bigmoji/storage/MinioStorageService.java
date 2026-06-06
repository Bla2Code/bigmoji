package com.bigmoji.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MinioStorageService {
  private final MinioClient minioClient;
  private final String bucketPrefix;

  public MinioStorageService(
      MinioClient minioClient,
      @Value("${bigmoji.minio.bucket-prefix:bigmoji-}") String bucketPrefix) {
    this.minioClient = minioClient;
    this.bucketPrefix = bucketPrefix;
  }

  public String upload(
      String guildId, String extension, InputStream inputStream, long size, String contentType)
      throws Exception {
    String bucket = bucketPrefix + guildId;
    ensureBucket(bucket);
    String objectKey = UUID.randomUUID() + extension;
    minioClient.putObject(
        PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(inputStream, size, -1)
            .contentType(contentType)
            .build());
    return objectKey;
  }

  public void delete(String bucket, String objectKey) throws Exception {
    minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
  }

  public String presignedGetUrl(String bucket, String objectKey) throws Exception {
    return minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .method(Method.GET)
            .bucket(bucket)
            .object(objectKey)
            .expiry(300)
            .build());
  }

  public String bucketForGuild(String guildId) {
    return bucketPrefix + guildId;
  }

  private void ensureBucket(String bucket) throws Exception {
    boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
    if (!exists) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }
  }
}
